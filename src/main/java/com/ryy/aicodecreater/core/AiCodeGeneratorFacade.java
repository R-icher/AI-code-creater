package com.ryy.aicodecreater.core;

import com.ryy.aicodecreater.ai.AiCodeGeneratorService;
import com.ryy.aicodecreater.ai.AiCodeGeneratorServiceFactory;
import com.ryy.aicodecreater.ai.model.HtmlCodeResult;
import com.ryy.aicodecreater.ai.model.MultiFileCodeResult;
import com.ryy.aicodecreater.core.parser.CodeParserExecutor;
import com.ryy.aicodecreater.core.saver.CodeFileSaverExecutor;
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
 *
 * 在当前设计中，该类主要整合了以下几个模块：
 * 1. AiCodeGeneratorService：负责调用大模型生成代码
 * 2. CodeParserExecutor：负责根据生成类型解析代码内容
 * 3. CodeFileSaverExecutor：负责根据生成类型保存代码文件
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    /**
     * AI 代码生成服务
     */
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据代码生成类型生成并保存代码（非流式）
     *
     * 处理流程如下：
     * 1. 校验生成类型是否为空
     * 2. 根据不同代码类型调用对应的大模型生成方法
     * 3. 获取生成结果对象
     * 4. 调用文件保存执行器，将结果保存到本地目录
     * 5. 返回保存目录
     *
     * 这里通过 switch 对不同代码类型进行分发，
     * 使调用方无需直接依赖具体生成实现和具体保存实现。
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 代码保存后的目录文件对象
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId, String outputDirPath) {
        // 校验生成类型不能为空，否则无法确定使用哪种生成和保存逻辑
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        // 根据不同生成类型，调用对应的代码生成逻辑和文件保存逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 调用 AI 服务生成 HTML 单文件代码结果
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);

                // 使用 文件保存执行器 统一保存 HTML 代码【根据传入不同的代码生成类型，自动跳转到对应的保存方法】
                // 区别于 return 直接返回一个方法的最终结果，yield 是当前 switch 分支的返回方式
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId, outputDirPath);
            }
            case MULTI_FILE -> {
                // 调用 AI 服务生成多文件代码结果
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);

                // 使用 文件保存执行器 统一保存多文件代码【根据传入不同的代码生成类型，自动跳转到对应的保存方法】
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId, outputDirPath);
            }
            default -> {
                // 如果传入的生成类型系统暂不支持，则抛出业务异常
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据代码生成类型生成并保存代码（流式）
     *
     * 该方法用于处理流式代码生成场景，
     * 例如前端需要边生成边展示代码内容时使用。
     *
     * 处理流程如下：
     * 1. 校验生成类型是否为空
     * 2. 根据不同代码类型调用对应的大模型流式生成方法
     * 3. 将生成得到的代码流交给 processCodeStream 统一处理
     * 4. 在流式返回结束后自动完成 代码解析 与 文件保存
     *
     * 与非流式方法的区别：
     * - 非流式：一次性拿到结果对象后直接保存
     * - 流式：先逐步返回代码片段，结束后再统一解析和保存
     *
     * @param userMessage 用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @return 流式代码内容，供前端实时消费
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId, String outputDirPath) {
        // 校验生成类型不能为空
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }

        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        // 根据不同生成类型，调用对应的流式代码生成逻辑
        return switch (codeGenTypeEnum) {
            case HTML -> {
                // 调用 AI 服务流式生成 HTML 代码
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);

                // 对流式代码进行统一处理：收集、解析、保存
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId, outputDirPath);
            }
            case MULTI_FILE -> {
                // 调用 AI 服务流式生成多文件代码
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);

                // 对流式代码进行统一处理：收集、解析、保存
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId, outputDirPath);
            }
            default -> {
                // 如果传入的生成类型系统暂不支持，则抛出业务异常
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * 通用流式代码处理方法
     *
     * 该方法用于统一处理流式代码生成后的后置逻辑，
     * 属于门面类内部的公共处理方法。
     *
     * 主要职责：
     * 1. 在流式返回过程中实时拼接每一段代码片段
     * 2. 在流式生成完成后，将完整代码内容进行统一解析
     * 3. 根据代码生成类型调用对应的保存器完成文件落盘
     * 4. 记录保存成功或失败日志
     *
     * 说明：
     * 流式场景下，大模型会持续返回代码片段，
     * 因此不能像普通生成那样一次性拿到完整结果对象。
     * 所以需要先用 StringBuilder 将所有片段拼接成完整代码，
     * 再在流结束时进行解析和保存。
     *
     * @param codeStream 代码流，每个元素表示一段代码片段
     * @param codeGenType 代码生成类型，用于决定后续解析器和保存器的选择
     * @return 原始流式响应，供前端或调用方继续消费
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId,String outputDirPath) {
        // 用于缓存流式返回的完整代码内容
        StringBuilder codeBuilder = new StringBuilder();

        return codeStream.doOnNext(chunk -> {
            // 在流式返回过程中，持续收集每一段代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 当流式响应结束后，说明完整代码已经生成完毕
            try {
                // 获取拼接后的完整代码内容
                String completeCode = codeBuilder.toString();

                // 使用 代码解析执行器，根据 代码类型 选择对应 解析器，
                // 将原始代码文本解析为结构化结果对象
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);

                // 使用 文件保存执行器，根据 代码类型 选择对应 保存器，
                // 将解析后的结果对象保存为本地文件
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId, outputDirPath);

                // 记录保存成功日志，方便后续排查和追踪
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                // 流式返回不应因保存失败而中断前端展示，因此这里只记录异常日志
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }
}
