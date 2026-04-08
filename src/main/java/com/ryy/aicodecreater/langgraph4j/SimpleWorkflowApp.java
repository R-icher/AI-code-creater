package com.ryy.aicodecreater.langgraph4j;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


/**
 * 简化版网站生成工作流应用
 *
 * 这段代码演示了如何使用 LangGraph4j 构建一个最基础的线性工作流。
 *
 * 整个工作流的执行顺序为：
 * 开始 -> 获取图片素材 -> 增强提示词 -> 智能路由选择 -> 网站代码生成 -> 项目构建 -> 结束
 */
@Slf4j
public class SimpleWorkflowApp {

    /**
     * 创建一个通用的工作节点
     *
     * 作用：
     * 1. 打印当前节点执行日志
     * 2. 返回当前节点产生的消息内容
     *
     * 这里为了简化代码，把多个节点的公共逻辑提取成了一个方法。
     * 只需要传入不同的 message，就能快速创建不同的节点。
     *
     * @param message 当前节点要输出的消息
     * @return 异步节点动作
     */
    static AsyncNodeAction<MessagesState<String>> makeNode(String message) {
        return node_async(state -> {
            // 打印当前执行到哪个节点
            log.info("执行节点: {}", message);

            // 返回节点执行后的结果
            // "messages" 是 MessagesState 中的核心字段
            // 这里相当于把当前节点的输出写入状态中
            return Map.of("messages", message);
        });
    }

    /**
     * 程序入口
     *
     * 主要流程：
     * 1. 创建工作流图
     * 2. 添加各个节点
     * 3. 定义节点之间的执行顺序
     * 4. 编译为可执行工作流
     * 5. 输出流程图结构
     * 6. 按步骤执行整个工作流
     *
     * @param args 启动参数
     * @throws GraphStateException 图状态异常
     */
    public static void main(String[] args) throws GraphStateException {

        // 创建并编译一个基于 MessagesState 的工作流
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()

                // 添加节点：图片收集
                .addNode("image_collector", makeNode("获取图片素材"))
                // 添加节点：提示词增强
                .addNode("prompt_enhancer", makeNode("增强提示词"))
                // 添加节点：智能路由
                .addNode("router", makeNode("智能路由选择"))
                // 添加节点：代码生成
                .addNode("code_generator", makeNode("网站代码生成"))
                // 添加节点：项目构建
                .addNode("project_builder", makeNode("项目构建"))

                // 添加边：开始 -> 图片收集
                .addEdge(START, "image_collector")
                // 添加边：图片收集 -> 提示词增强
                .addEdge("image_collector", "prompt_enhancer")
                // 添加边：提示词增强 -> 智能路由
                .addEdge("prompt_enhancer", "router")
                // 添加边：智能路由 -> 代码生成
                .addEdge("router", "code_generator")
                // 添加边：代码生成 -> 项目构建
                .addEdge("code_generator", "project_builder")
                // 添加边：项目构建 -> 结束
                .addEdge("project_builder", END)

                // 编译工作流
                // compile() 的作用是把前面定义好的节点和边转换成真正可执行的图对象
                .compile();

        log.info("开始执行工作流");

        // 获取当前工作流的图结构表示
        // 这里使用 Mermaid 格式，方便后续可视化展示
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);

        // 打印 Mermaid 格式的流程图文本
        log.info("工作流图: \n{}", graph.content());

        // 记录当前执行到第几步
        int stepCounter = 1;
        // 开始执行工作流
        // workflow.stream(Map.of()) 表示以一个空状态作为初始输入，逐步执行整个流程
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of())) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 打印当前步骤的输出结果
            // step 中包含当前节点执行后的状态信息
            log.info("步骤输出: {}", step);
            stepCounter++;
        }

        log.info("工作流执行完成！");
    }
}
