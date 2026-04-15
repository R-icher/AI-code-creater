package com.ryy.aicodecreater.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ryy.aicodecreater.model.entity.AppVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用版本 Mapper
 *
 * 用于操作 app_version 表
 */
@Mapper
public interface AppVersionMapper extends BaseMapper<AppVersion> {
}