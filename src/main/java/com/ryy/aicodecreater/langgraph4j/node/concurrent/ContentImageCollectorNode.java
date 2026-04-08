package com.ryy.aicodecreater.langgraph4j.node.concurrent;

import com.ryy.aicodecreater.langgraph4j.model.ImageCollectionPlan;
import com.ryy.aicodecreater.langgraph4j.model.ImageResource;
import com.ryy.aicodecreater.langgraph4j.state.WorkflowContext;
import com.ryy.aicodecreater.langgraph4j.tools.ImageSearchTool;
import com.ryy.aicodecreater.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ContentImageCollectorNode {

    /**
     * 创建“内容图片收集”节点
     *
     * 节点职责：
     * 1. 从工作流状态 state 中取出 WorkflowContext
     * 2. 从上下文中获取前面步骤已经生成好的图片收集计划 ImageCollectionPlan
     * 3. 遍历计划中的内容图片搜索任务
     * 4. 调用 ImageSearchTool 搜索内容图片
     * 5. 将搜索到的图片结果暂存到 context 的中间字段 contentImages 中
     * 6. 更新当前工作流步骤，并将 context 重新保存回 state
     *
     * @return 异步节点动作
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {

            // 1. 从当前工作流状态中获取上下文对象
            WorkflowContext context = WorkflowContext.getContext(state);

            // 2. 用于存放当前节点收集到的“内容图片”
            // 这里先创建一个空列表，后面把每个任务搜索出来的图片统一汇总到这里
            List<ImageResource> contentImages = new ArrayList<>();

            try {
                // 3. 从上下文中获取图片收集计划
                // 这个 plan 一般是在前面的“图片计划生成节点”中提前生成好的
                ImageCollectionPlan plan = context.getImageCollectionPlan();

                // 4. 做空值判断：
                if (plan != null && plan.getContentImageTasks() != null) {

                    // 5. 从 Spring 容器中获取图片搜索工具
                    // 因为当前类通常不是普通的 Spring Bean 注入场景，所以通过工具类手动获取
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);

                    // 6. 记录日志：准备开始执行内容图片收集任务
                    log.info("开始并发收集内容图片，任务数: {}", plan.getContentImageTasks().size());

                    // 7. 遍历所有内容图片搜索任务
                    // 每一个 task 都对应一条图片搜索指令，比如：
                    // “医院场景图片”、“宠物医生看诊图片”、“首页 banner 图片”等
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {

                        // 8. 调用图片搜索工具，根据 task 中的 query 搜索内容图片
                        // 例如 task.query() 可能是："pet doctor examining dog in clinic"
                        List<ImageResource> images = imageSearchTool.searchContentImages(task.query());

                        // 9. 如果本次搜索结果不为空，则将结果追加到总列表中
                        if (images != null) {
                            contentImages.addAll(images);
                        }
                    }

                    // 10. 所有内容图片任务执行完成后，记录最终收集到的图片数量
                    log.info("内容图片收集完成，共收集到 {} 张图片", contentImages.size());
                }
            } catch (Exception e) {
                log.error("内容图片收集失败: {}", e.getMessage(), e);
            }

            // 12. 将当前节点收集到的内容图片保存到上下文中
            // 注意：这里保存的是“中间结果字段”，供后续节点继续处理
            context.setContentImages(contentImages);
            // 13. 更新当前工作流步骤，表示当前已经执行到“内容图片收集”阶段
            context.setCurrentStep("内容图片收集");

            // 14. 将更新后的上下文重新保存回工作流状态，并返回给下一个节点
            return WorkflowContext.saveContext(context);
        });
    }
}
