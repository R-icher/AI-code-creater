package com.ryy.aicodecreater.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ryy.aicodecreater.model.entity.User;
import com.ryy.aicodecreater.mapper.UserMapper;
import com.ryy.aicodecreater.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/R-icher">Richer</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

}
