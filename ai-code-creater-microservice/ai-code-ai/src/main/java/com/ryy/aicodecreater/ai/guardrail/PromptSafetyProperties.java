package com.ryy.aicodecreater.ai.guardrail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt 安全护轨配置
 *
 * 配置来源：
 * 1. 本地 application.yml
 * 2. Nacos 配置中心
 *
 * 作用：
 * - 将敏感词、注入检测正则、最大长度等规则配置化
 * - 配合 @RefreshScope 实现动态刷新
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "ai.guardrail.prompt-safety")
public class PromptSafetyProperties {

    /**
     * 是否开启护轨
     */
    private boolean enabled = true;

    /**
     * 输入最大长度
     */
    private int maxLength = 1000;

    /**
     * 敏感词列表
     */
    private List<String> sensitiveWords = new ArrayList<>();

    /**
     * 注入攻击检测正则
     */
    private List<String> injectionPatterns = new ArrayList<>();
}