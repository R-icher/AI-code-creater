package com.ryy.aicodecreater.langgraph4j.node;

import com.ryy.aicodecreater.langgraph4j.ai.ImageCollectionService;
import com.ryy.aicodecreater.langgraph4j.model.ImageResource;
import com.ryy.aicodecreater.langgraph4j.model.enums.ImageCategoryEnum;
import com.ryy.aicodecreater.langgraph4j.state.WorkflowContext;
import com.ryy.aicodecreater.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            
            // TODO: 实际执行图片收集逻辑
            // 获取到原始 prompt
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = "";
            try {
                // 获取 图片收集服务 AI Service
                ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
                // 使用 AI 服务进行智能图片收集
                imageListStr = imageCollectionService.collectImages(originalPrompt);
            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }
            
            // 更新状态
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageListStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
