package com.ryy.aicodecreater.langgraph4j;

import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.langgraph4j.model.QualityResult;
import com.ryy.aicodecreater.langgraph4j.node.*;
import com.ryy.aicodecreater.langgraph4j.state.WorkflowContext;
import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 代码生成工作流
 *
 * 作用：
 * 1. 负责构建完整的代码生成流程图
 * 2. 负责执行工作流
 * 3. 将执行过程中的上下文状态逐步传递给各个节点
 * 4. 最终返回整个流程执行完成后的 WorkflowContext
 */
@Slf4j
public class CodeGenWorkflow {

    /**
     * 创建完整的工作流
     *
     * 工作流执行顺序：
     * START
     * -> image_collector（图片收集）
     * -> prompt_enhancer（提示词增强）
     * -> router（路由选择）
     * -> code_generator（代码生成）
     * -> project_builder（项目构建）
     * -> END
     *
     * @return 编译完成后的工作流图对象
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // 添加各个业务节点
                    // 每个节点都负责处理 WorkflowContext 中的一部分数据
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())

                    // 定义节点之间的执行顺序
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")

                    // 新增质检条件边：根据质检结果决定下一步
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",   // 质检通过且需要构建
                                    "skip_build", END,            // 质检通过但跳过构建
                                    "fail", "code_generator"      // 质检失败，重新生成
                            ))

                    .addEdge("project_builder", END)

                    // 编译工作流图
                    .compile();
        } catch (GraphStateException e) {
            // 如果工作流图构建失败，则包装成系统自己的业务异常抛出
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    /**
     * 执行工作流
     *
     * 执行流程：
     * 1. 创建完整工作流
     * 2. 初始化上下文 WorkflowContext
     * 3. 将初始上下文传入工作流
     * 4. 按节点顺序逐步执行
     * 5. 每执行完一个节点，就从当前状态中取出最新的上下文
     * 6. 最终返回最后一个节点执行完成后的上下文结果
     *
     * @param originalPrompt 用户输入的原始提示词
     * @return 工作流执行完成后的最终上下文
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 先创建工作流图
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化工作流上下文
        // 这里只放入最基础的数据：用户原始提示词 + 当前步骤
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        // 获取工作流图的 Mermaid 表示，方便打印查看整体结构
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());
        log.info("开始执行代码生成工作流");

        // finalContext 用于保存流程执行过程中的最新上下文
        // 最终返回的就是最后一步执行完成后的上下文
        WorkflowContext finalContext = null;

        // 记录当前执行到第几步，方便打印日志观察执行过程
        int stepCounter = 1;

        // workflow.stream(...) 会按节点顺序依次执行工作流
        // 这里将初始的 WorkflowContext 放入状态中，作为整个流程的起点
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {

            log.info("--- 第 {} 步完成 ---", stepCounter);

            // 从当前节点执行完成后的状态中，取出最新的 WorkflowContext
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());

            if (currentContext != null) {
                // 持续更新 finalContext，确保最后拿到的是最终执行结果
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }

            stepCounter++;
        }

        log.info("代码生成工作流执行完成！");

        // 返回最终执行结果
        return finalContext;
    }


    /**
     * 路由函数决定代码生成后是否需要项目构建
     * @param state
     * @return
     */
    private String routeBuildOrSkip(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenTypeEnum generationType = context.getGenerationType();
        // HTML 和 MULTI_FILE 类型不需要构建，直接结束
        if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
            return "skip_build";
        }
        // VUE_PROJECT 需要构建
        return "build";
    }


    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        // 如果质检失败，重新生成代码
        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }
        // 质检通过，复用原有的构建路由逻辑
        log.info("代码质检通过，继续后续流程");
        return routeBuildOrSkip(state);
    }

}
