package com.ryy.aicodecreater.ai;

import com.ryy.aicodecreater.ai.model.HtmlCodeResult;
import com.ryy.aicodecreater.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户需求
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     * @param userMessage 用户需求
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}