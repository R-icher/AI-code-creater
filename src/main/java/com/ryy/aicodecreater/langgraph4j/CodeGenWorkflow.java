package com.ryy.aicodecreater.langgraph4j;

import cn.hutool.json.JSONUtil;
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
import reactor.core.publisher.Flux;

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
     * 执行工作流（Flux 流式输出版本）
     *
     * 整体流程：
     * 1. 创建一个 Flux 流，向前端持续推送工作流执行过程中的事件
     * 2. 在虚拟线程中执行工作流，避免阻塞当前调用线程
     * 3. 初始化工作流上下文，并发送“工作流开始”事件
     * 4. 按步骤遍历工作流执行结果，每完成一步就推送一次进度事件
     * 5. 全部执行完成后，推送“工作流完成”事件
     * 6. 如果中途出错，则推送“工作流失败”事件
     *
     * @param originalPrompt 用户输入的原始提示词
     * @return Flux<String> 以 SSE 字符串格式返回的流式结果
     */
    public Flux<String> executeWorkflowWithFlux(String originalPrompt) {
        return Flux.create(sink -> {
            // 使用虚拟线程异步执行工作流
            // 这样可以避免阻塞当前请求线程，同时适合处理这类耗时流程
            Thread.startVirtualThread(() -> {
                try {
                    // 1. 创建编译后的工作流对象
                    // createWorkflow() 会构建并返回完整的工作流定义
                    CompiledGraph<MessagesState<String>> workflow = createWorkflow();

                    // 2. 构造初始工作流上下文
                    // 这里将用户的原始提示词和初始步骤信息放入上下文中
                    WorkflowContext initialContext = WorkflowContext.builder()
                            .originalPrompt(originalPrompt)
                            .currentStep("初始化")
                            .build();

                    // 3. 向前端推送“工作流开始执行”的 SSE 事件
                    sink.next(formatSseEvent("workflow_start", Map.of(
                            "message", "开始执行代码生成工作流",
                            "originalPrompt", originalPrompt
                    )));

                    // 4. 获取工作流结构图（Mermaid 格式）
                    // 主要用于日志输出，方便开发时查看整个工作流拓扑结构
                    GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                    log.info("工作流图:\n{}", graph.content());

                    // 5. 定义步骤计数器，用于记录当前执行到第几步
                    int stepCounter = 1;

                    // 6. 流式遍历工作流的每一步执行结果
                    // workflow.stream(...) 会按照工作流执行顺序，逐步返回每个节点执行后的状态
                    for (NodeOutput<MessagesState<String>> step : workflow.stream(
                            Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {

                        log.info("--- 第 {} 步完成 ---", stepCounter);

                        // 7. 从当前步骤返回的 state 中取出最新的工作流上下文
                        // 这样就能拿到当前步骤名称、阶段结果等信息
                        WorkflowContext currentContext = WorkflowContext.getContext(step.state());

                        // 8. 如果当前上下文不为空，则推送“步骤完成”事件给前端
                        if (currentContext != null) {
                            sink.next(formatSseEvent("step_completed", Map.of(
                                    "stepNumber", stepCounter,
                                    "currentStep", currentContext.getCurrentStep()
                            )));

                            // 记录当前步骤的上下文信息，方便排查问题
                            log.info("当前步骤上下文: {}", currentContext);
                        }

                        // 9. 步骤计数器递增
                        stepCounter++;
                    }

                    // 10. 工作流全部执行完成后，推送“完成”事件
                    sink.next(formatSseEvent("workflow_completed", Map.of(
                            "message", "代码生成工作流执行完成！"
                    )));

                    log.info("代码生成工作流执行完成！");

                    // 11. 正常结束 Flux 流
                    sink.complete();
                } catch (Exception e) {
                    log.error("工作流执行失败: {}", e.getMessage(), e);
                    sink.next(formatSseEvent("workflow_error", Map.of(
                            "error", e.getMessage(),
                            "message", "工作流执行失败"
                    )));
                    sink.error(e);
                }
            });
        });
    }


    /**
     * 格式化 SSE 事件的辅助方法
     *
     * SSE（Server-Sent Events）协议的基本格式如下：
     * event: 事件名
     * data: 事件数据
     *
     * 每条事件之间需要以空行分隔，因此最后要加两个换行符 \n\n
     *
     * 例如：
     * event: step_completed
     * data: {"stepNumber":1,"currentStep":"初始化"}
     *
     * @param eventType 事件类型，例如 workflow_start / step_completed / workflow_completed
     * @param data      事件携带的数据对象，会被序列化为 JSON 字符串
     * @return 符合 SSE 协议格式的字符串
     */
    private String formatSseEvent(String eventType, Object data) {
        try {
            // 1. 将传入的数据对象序列化为 JSON 字符串
            String jsonData = JSONUtil.toJsonStr(data);

            // 2. 按照 SSE 协议格式拼接事件内容
            // event: 指定事件类型
            // data: 指定事件数据
            // \n\n 表示一条完整事件结束
            return "event: " + eventType + "\ndata: " + jsonData + "\n\n";
        } catch (Exception e) {
            // 3. 如果 JSON 序列化失败，则返回一个默认的错误事件
            log.error("格式化 SSE 事件失败: {}", e.getMessage(), e);
            return "event: error\ndata: {\"error\":\"格式化失败\"}\n\n";
        }
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
