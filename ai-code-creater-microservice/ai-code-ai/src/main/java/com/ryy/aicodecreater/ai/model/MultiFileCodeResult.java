package com.ryy.aicodecreater.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成多个前端代码文件的结果")
@Data
public class MultiFileCodeResult {

    @Description("HTML 代码内容")
    private String htmlCode;

    @Description("CSS 代码内容")
    private String cssCode;

    @Description("JavaScript 代码内容")
    private String jsCode;

    @Description("对生成代码的说明")
    private String description;
}