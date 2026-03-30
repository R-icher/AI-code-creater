package com.ryy.aicodecreater.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建应用请求
 * 用户创建应用时，只需要填写初始化提示词。系统会自动生成应用名称（取提示词前 12 ‍位）和默认的代码生成类型。
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}

