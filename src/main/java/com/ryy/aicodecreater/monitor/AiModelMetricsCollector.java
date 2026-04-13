package com.ryy.aicodecreater.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class AiModelMetricsCollector {

    /**
     * Micrometer 的指标注册中心
     *
     * 所有 Counter、Timer 等指标最终都会注册到这里，
     * 之后可以被 Prometheus、Grafana 等监控系统采集。
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 请求次数指标缓存
     *
     * key 由 userId、appId、modelName、status 组成，
     * value 是对应的 Counter。
     *
     * 作用：
     * 避免每次记录指标时都重新创建 Counter，
     * 提高性能，并保证同一组标签对应同一个指标对象。
     */
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();

    /**
     * 错误次数指标缓存
     *
     * key 由 userId、appId、modelName、errorMessage 组成，
     * value 是对应的 Counter。
     *
     * 作用：
     * 针对不同错误信息分别统计出现次数。
     */
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();

    /**
     * Token 消耗指标缓存
     *
     * key 由 userId、appId、modelName、tokenType 组成，
     * value 是对应的 Counter。
     *
     * 作用：
     * 用于分别统计不同类型 Token 的累计消耗量，
     * 例如 input_token、output_token 等。
     */
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();

    /**
     * 响应时间指标缓存
     *
     * key 由 userId、appId、modelName 组成，
     * value 是对应的 Timer。
     *
     * 作用：
     * 用于统计某个用户、某个应用、某个模型的响应耗时情况。
     */
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();


    /**
     * 记录请求次数
     *
     * @param userId    用户 ID
     * @param appId     应用 ID
     * @param modelName 模型名称
     * @param status    请求状态，例如 success / fail
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        // 使用标签拼接成唯一 key，用于从缓存中获取或创建 Counter
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status);

        // 如果缓存中不存在，则创建一个新的 Counter 并注册到 meterRegistry
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_requests_total")
                        .description("AI模型总请求次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("status", status)
                        .register(meterRegistry)
        );

        // 请求次数 +1
        counter.increment();
    }


    /**
     * 记录错误次数
     *
     * @param userId       用户 ID
     * @param appId        应用 ID
     * @param modelName    模型名称
     * @param errorMessage 错误信息
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        // 用用户、应用、模型、错误信息生成唯一 key
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);

        // 如果缓存中没有对应 Counter，就创建并注册
        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_errors_total")
                        .description("AI模型错误次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("error_message", errorMessage)
                        .register(meterRegistry)
        );

        // 错误次数 +1
        counter.increment();
    }


    /**
     * 记录 Token 消耗总量
     *
     * @param userId     用户 ID
     * @param appId      应用 ID
     * @param modelName  模型名称
     * @param tokenType  Token 类型，例如 input / output / total
     * @param tokenCount 本次消耗的 Token 数量
     */
    public void recordTokenUsage(String userId, String appId, String modelName,
                                 String tokenType, long tokenCount) {
        // 使用用户、应用、模型、Token 类型构造唯一 key
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);

        // 如果缓存中没有，则创建并注册 Counter
        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_tokens_total")
                        .description("AI模型Token消耗总数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("token_type", tokenType)
                        .register(meterRegistry)
        );

        // 将本次 token 消耗量累加到 Counter 中
        counter.increment(tokenCount);
    }


    /**
     * 记录模型响应时间
     *
     * @param userId    用户 ID
     * @param appId     应用 ID
     * @param modelName 模型名称
     * @param duration  本次请求耗时
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        // 以用户、应用、模型维度构造唯一 key
        String key = String.format("%s_%s_%s", userId, appId, modelName);

        // 如果缓存中没有对应 Timer，则创建并注册
        Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_duration_seconds")
                        .description("AI模型响应时间")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .register(meterRegistry)
        );

        // 记录本次请求耗时
        timer.record(duration);
    }
}