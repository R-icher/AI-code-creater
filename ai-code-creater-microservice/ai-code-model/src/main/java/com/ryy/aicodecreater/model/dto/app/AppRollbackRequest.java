package com.ryy.aicodecreater.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppRollbackRequest implements Serializable {
    private Long appId;
    /** 目标回退版本号 */
    private Integer targetVersion;
    private static final long serialVersionUID = 1L;
}