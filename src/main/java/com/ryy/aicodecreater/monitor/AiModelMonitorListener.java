package com.ryy.aicodecreater.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI 模型调用监控埋点监听器
 */
@Component
@Slf4j
public class AiModelMonitorListener implements ChatModelListener {

    // 用于存储请求开始时间的键
    private static final String REQUEST_START_TIME_KEY = "request_start_time";
    // 用于监控上下文传递（因为请求和响应事件的触发不是同一个线程）
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;


    /**
     * 请求开始时：记录开始时间、记录一次“请求已发起”
     * @param requestContext
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        // 获取当前时间戳，但未作任何处理
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
        // 从监控上下文中获取信息
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);
        // 获取模型名称
        String modelName = requestContext.chatRequest().modelName();
        // 记录请求指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
    }


    /**
     * 响应成功时：记录一次“请求成功”、统计耗时、统计 token 使用量
     * @param responseContext
     */
    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 从属性中获取监控信息（由 onRequest 方法存储）
        Map<Object, Object> attributes = responseContext.attributes();
        // 从监控上下文中获取信息
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        String userId = context.getUserId();
        String appId = context.getAppId();
        // 获取模型名称
        String modelName = responseContext.chatResponse().modelName();
        // 记录成功请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");
        // 记录响应时间
        recordResponseTime(attributes, userId, appId, modelName);
        // 记录 Token 使用情况
        recordTokenUsage(responseContext, userId, appId, modelName);
    }


    /**
     * 响应失败时：记录一次“请求失败”、记录错误信息、统计失败耗时
     * @param errorContext
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // 从监控上下文中获取信息
        MonitorContext context = MonitorContextHolder.getContext();
        String userId = context.getUserId();
        String appId = context.getAppId();
        // 获取模型名称和错误类型
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();
        // 记录失败请求
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);
        // 记录响应时间（即使是错误响应）
        Map<Object, Object> attributes = errorContext.attributes();
        recordResponseTime(attributes, userId, appId, modelName);
    }


    /**
     * 记录模型响应时间
     *
     * 处理流程：
     * 1. 从 attributes 中取出请求开始时间
     * 2. 计算从开始到当前的时间差
     * 3. 上报到指标采集器
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        // 获取请求开始时间
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        // 计算本次请求耗时
        Duration responseTime = Duration.between(startTime, Instant.now());
        // 上报响应时间指标
        aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
    }


    /**
     * 记录 Token 使用情况
     *
     * 处理流程：
     * 1. 从响应元数据中获取 TokenUsage
     * 2. 分别记录 input / output / total token 数量
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
        // 从响应元数据中获取 Token 使用情况
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();

        // 如果模型返回了 token 统计信息，则分别记录
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }
}