package com.ryy.aicodecreater.core;

import com.ryy.aicodecreater.ai.AiCodeGeneratorService;
import com.ryy.aicodecreater.ai.model.HtmlCodeResult;
import com.ryy.aicodecreater.ai.model.MultiFileCodeResult;
import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成门面类
 * <p>
 * 作用：
 * 1. 作为“门面模式”的统一入口，对外屏蔽内部复杂流程
 * 2. 负责协调 AI 代码生成服务 和 文件保存工具类
 * 3. 调用方只需要传入用户需求和生成类型，即可完成“生成代码 + 保存文件”的完整流程
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    /**
     * AI 代码生成服务
     * 负责调用大模型，根据用户提示词生成对应的代码结果。
     */
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据生成类型生成代码并保存到本地
     * <p>
     * 处理流程：
     * 1. 校验生成类型是否为空
     * 2. 根据不同生成类型，调用不同的代码生成方法
     * 3. 将生成结果写入本地文件
     * 4. 返回保存后的目录
     *
     * @param userMessage     用户输入的提示词，用于告诉 AI 要生成什么内容
     * @param codeGenTypeEnum 代码生成类型，例如：HTML 模式、多文件模式
     * @return 保存生成代码的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        // 如果生成类型为空，直接抛出业务异常
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 根据生成类型，分发到不同的生成和保存逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            // 其他类型暂不支持
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     * <p>
     * 处理流程：
     * 1. 调用 AI 服务生成 HTML 结果
     * 2. 将生成结果保存为本地文件
     * 3. 返回保存目录
     *
     * @param userMessage 用户提示词
     * @return 保存生成结果的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        // 调用 AI 生成 HTML 模式代码
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        // 调用之前的保存文件到本地的代码 CodeFileSaver，并返回目录对象
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     * <p>
     * 处理流程：
     * 1. 调用 AI 服务生成多文件代码结果
     * 2. 将 HTML、CSS、JS 分别保存到本地文件
     * 3. 返回保存目录
     *
     * @param userMessage 用户提示词
     * @return 保存生成结果的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        // 调用 AI 生成多文件模式代码
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        // 调用之前的保存文件到本地的代码 CodeFileSaver，并返回目录对象
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }


    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 生成 HTML 模式的代码并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        // 需要一个字符串拼接器，用于当流式返回所有的代码之后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunk -> {
            // 实时收集并拼接代码片段
            codeBuilder.append(chunk);
            // 当触发了 doOnComplete() 方法，就代表 AI 已经将所有内容生成完毕了
        }).doOnComplete(() -> {
            try {
                // 当流式返回所有代码之后，保存代码
                String completeHtmlCode = codeBuilder.toString();
                // 解析出 Html 代码部分
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                // 调用文件保存
                File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }


    /**
     * 生成多文件模式的代码并保存（流式）
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        // 当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return result.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeMultiFileCode = codeBuilder.toString();
                MultiFileCodeResult multiFileResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                // 保存代码到文件
                File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileResult);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }
}
