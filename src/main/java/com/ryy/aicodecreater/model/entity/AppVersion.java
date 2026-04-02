package com.ryy.aicodecreater.model.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app_version")
public class AppVersion implements Serializable {

    private Long id;
    private Long appId;
    private Integer version;
    private String prompt;
    private String sourceDirPath;
    private Date createTime;
    private Date updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}