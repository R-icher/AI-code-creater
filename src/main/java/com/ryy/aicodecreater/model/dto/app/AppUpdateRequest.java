package com.ryy.aicodecreater.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新应用请求
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
