package com.ryy.aicodecreater.langgraph4j.node;

import com.ryy.aicodecreater.langgraph4j.ai.ImageCollectionPlanService;
import com.ryy.aicodecreater.langgraph4j.ai.ImageCollectionService;
import com.ryy.aicodecreater.langgraph4j.model.ImageCollectionPlan;
import com.ryy.aicodecreater.langgraph4j.model.ImageResource;
import com.ryy.aicodecreater.langgraph4j.model.enums.ImageCategoryEnum;
import com.ryy.aicodecreater.langgraph4j.state.WorkflowContext;
import com.ryy.aicodecreater.langgraph4j.tools.ImageSearchTool;
import com.ryy.aicodecreater.langgraph4j.tools.LogoGeneratorTool;
import com.ryy.aicodecreater.langgraph4j.tools.MermaidDiagramTool;
import com.ryy.aicodecreater.langgraph4j.tools.UndrawIllustrationTool;
import com.ryy.aicodecreater.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {

    /**
     * 创建图片收集节点
     *
     * 节点职责：
     * 1. 从工作流状态中获取用户原始提示词
     * 2. 调用图片收集计划服务，先生成一份“图片收集计划”
     * 3. 根据计划并发执行不同类型的图片收集任务
     * 4. 汇总所有收集到的图片资源
     * 5. 将结果更新回 WorkflowContext，供后续节点继续使用
     *
     * @return 异步工作节点动作
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 从当前工作流状态中取出上下文对象
            WorkflowContext context = WorkflowContext.getContext(state);

            // 获取用户最初输入的提示词，后续会根据它来规划图片收集任务
            String originalPrompt = context.getOriginalPrompt();

            // 用于保存最终收集到的所有图片资源
            List<ImageResource> collectedImages = new ArrayList<>();

            try {
                // 第一步：先获取图片收集计划服务
                // 由于当前节点是通过静态方法创建的，不能直接注入 Bean，所以通过 SpringContextUtil 手动从 Spring 容器中获取
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);

                // 根据用户原始提示词，调用图片收集 AIService，生成图片收集计划
                // 这个计划中会包含多种任务，例如：
                // 1. 内容图片搜索任务
                // 2. 插画搜索任务
                // 3. 架构图生成任务
                // 4. Logo 生成任务
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 第二步：并发执行各种图片收集任务
                // futures 用来保存每一个异步任务，每个异步任务最终都会返回一个 List<ImageResource>
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();

                // -------------------- 并发执行内容图片搜索 --------------------
                // 如果计划中存在内容图片搜索任务，则逐个创建异步任务
                if (plan.getContentImageTasks() != null) {
                    // 获取内容图片搜索工具
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    // 遍历所有内容图片搜索任务
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        // 为每个任务创建一个异步执行单元
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }

                // -------------------- 并发执行插画图片搜索 --------------------
                // 如果计划中存在插画任务，则逐个异步执行
                if (plan.getIllustrationTasks() != null) {
                    // 获取插画搜索工具
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    // 遍历所有插画任务
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        // 为每个任务创建一个异步执行单元
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }

                // -------------------- 并发执行架构图生成 --------------------
                // 如果计划中存在 Mermaid 架构图生成任务，则逐个异步执行
                if (plan.getDiagramTasks() != null) {
                    // 获取架构图生成工具
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    // 遍历所有架构图任务
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        // 每个任务都会根据 Mermaid 代码和描述生成架构图图片
                        futures.add(CompletableFuture.supplyAsync(() ->
                                diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }

                // -------------------- 并发执行 Logo 生成 --------------------
                // 如果计划中存在 Logo 生成任务，则逐个异步执行
                if (plan.getLogoTasks() != null) {
                    // 获取 Logo 生成工具
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    // 遍历所有 Logo 任务
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        // 每个任务都会根据描述生成 Logo 图片
                        futures.add(CompletableFuture.supplyAsync(() ->
                                logoTool.generateLogos(task.description())));
                    }
                }

                // 第三步：等待所有异步任务执行完成
                // CompletableFuture.allOf(...) 会把所有 future 组合成一个总任务
                // join() 表示阻塞等待，直到所有任务都执行结束
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                allTasks.join();

                // 第四步：收集所有异步任务的执行结果
                // 遍历每个 future，取出其中返回的图片列表
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();

                    // 如果当前任务返回的图片列表不为空，则追加到最终结果集中
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }

                log.info("并发图片收集完成，共收集到 {} 张图片", collectedImages.size());
            } catch (Exception e) {
                // 这里没有直接抛异常，是为了避免整个工作流因为图片收集失败而中断
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            // 第五步：更新工作流上下文
            // 记录当前节点名称，表示当前步骤已经执行到“图片收集”
            context.setCurrentStep("图片收集");

            // 将收集到的所有图片资源保存到上下文中
            // 供后续节点（如提示词增强、代码生成等）继续使用
            context.setImageList(collectedImages);

            // 将更新后的上下文重新保存回工作流状态中
            return WorkflowContext.saveContext(context);
        });
    }
}

