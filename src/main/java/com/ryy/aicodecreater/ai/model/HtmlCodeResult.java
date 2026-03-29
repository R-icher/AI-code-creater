package com.ryy.aicodecreater.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("HTML 代码内容")
    private String htmlCode;

    @Description("对生成代码的说明")
    private String description;
}