package com.ryy.aicodecreater.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Prompt 安全审查
 *
 * 改造点：
 * 1. 原来的敏感词和注入正则不再写死在代码中
 * 2. 改为从 PromptSafetyProperties 动态读取
 * 3. 配合 Nacos + @RefreshScope，实现规则热更新
 */
@Component
@RequiredArgsConstructor
public class PromptSafetyInputGuardrail implements InputGuardrail {

    /**
     * 护轨配置对象
     * 配置可来自 Nacos，并支持动态刷新
     */
    private final PromptSafetyProperties promptSafetyProperties;

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();

        // 1. 开关关闭时，直接放行
        if (!promptSafetyProperties.isEnabled()) {
            return success();
        }

        // 2. 检查是否为空
        if (input == null || input.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }

        // 3. 检查输入长度
        if (input.length() > promptSafetyProperties.getMaxLength()) {
            return fatal("输入内容过长，不要超过 " + promptSafetyProperties.getMaxLength() + " 字");
        }

        // 4. 检查敏感词
        String lowerInput = input.toLowerCase();
        List<String> sensitiveWords = promptSafetyProperties.getSensitiveWords();
        if (sensitiveWords != null) {
            for (String sensitiveWord : sensitiveWords) {
                if (sensitiveWord != null && !sensitiveWord.isBlank()
                        && lowerInput.contains(sensitiveWord.toLowerCase())) {
                    return fatal("输入包含不当内容，请修改后重试");
                }
            }
        }

        // 5. 检查注入攻击模式
        List<String> injectionPatterns = promptSafetyProperties.getInjectionPatterns();
        if (injectionPatterns != null) {
            for (String regex : injectionPatterns) {
                if (regex == null || regex.isBlank()) {
                    continue;
                }
                Pattern pattern = Pattern.compile(regex);
                if (pattern.matcher(input).find()) {
                    return fatal("检测到恶意输入，请求被拒绝");
                }
            }
        }

        return success();
    }
}
