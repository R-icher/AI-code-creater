package com.ryy.aicodecreater.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ryy.aicodecreater.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author <a href="https://github.com/R-icher">Richer</a>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
