package com.ryy.aicodecreater.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppFileContentRequest implements Serializable {
    private Long appId;
    private Integer version;
    /** 相对路径，例如 src/main/java/Main.java */
    private String relativeFilePath;
    private static final long serialVersionUID = 1L;
}