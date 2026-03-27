package com.ryy.aicodecreater.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.model.dto.user.UserQueryRequest;
import com.ryy.aicodecreater.model.entity.User;
import com.ryy.aicodecreater.mapper.UserMapper;
import com.ryy.aicodecreater.model.enums.UserRoleEnum;
import com.ryy.aicodecreater.model.vo.LoginUserVO;
import com.ryy.aicodecreater.model.vo.UserVO;
import com.ryy.aicodecreater.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ryy.aicodecreater.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/R-icher">Richer</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2. 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 3. 加密密码
        String encryptPassword = getEncryptPassword(userPassword);

        // 4. 创建用户，插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }


    @Override
    /**
     * 获取加密后的密码
     * @param userPassword 用户原始密码
     * @return 加密后的密码字符串
     */
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "yupi";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }


    /**
     * 将 User 类的属性复制到 LoginUserVO 中，防止将密码一类的敏感信息也返回给前端
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 4. 获得脱敏后的用户信息
        return this.getLoginUserVO(user);
    }


    /**
     * 获取当前登录用户信息的方法
     *
     * @param request HttpServletRequest对象，用于获取会话信息
     * @return User 当前登录用户对象
     * @throws BusinessException 当用户未登录或用户不存在时抛出异常
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return currentUser;
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


    /**
     * 将User对象转换为UserVO对象
     *
     * @param user 需要转换的User对象
     * @return 转换后的UserVO对象，如果输入为null则返回null
     */
    @Override
    public UserVO getUserVO(User user) {
        // 检查输入参数是否为null，如果是则直接返回null
        if (user == null) {
            return null;
        }
        // 创建UserVO对象实例
        UserVO userVO = new UserVO();
        // 使用BeanUtil工具类将user对象的属性拷贝到userVO对象中
        BeanUtil.copyProperties(user, userVO);
        // 返回转换后的UserVO对象
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList 用户实体列表
     * @return 用户视图对象列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        // 如果用户列表为空，则返回一个新的空列表
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        // 使用Stream流将用户实体列表转换为用户视图对象列表
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    /**
     * 将用户查询请求对象转换为 MyBatis-Flex 的 QueryWrapper 查询条件。
     *
     * @param userQueryRequest 用户查询请求，包含筛选条件与排序参数
     * @return 组装完成的 QueryWrapper 对象
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 1. 参数校验：请求对象不能为空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        // 2. 从请求对象中提取各个查询参数
        Long id = userQueryRequest.getId();                    // 用户ID（精确匹配）
        String userAccount = userQueryRequest.getUserAccount();// 用户账号（模糊匹配）
        String userName = userQueryRequest.getUserName();      // 用户名（模糊匹配）
        String userProfile = userQueryRequest.getUserProfile();// 用户简介（模糊匹配）
        String userRole = userQueryRequest.getUserRole();      // 用户角色（精确匹配）
        String sortField = userQueryRequest.getSortField();    // 排序字段
        String sortOrder = userQueryRequest.getSortOrder();    // 排序方式（ascend / descend）

        // 3. 构建并返回 QueryWrapper
        return QueryWrapper.create()
                .eq("id", id)                                  // 按 id 精确查询
                .eq("userRole", userRole)                      // 按用户角色精确查询
                .like("userAccount", userAccount)              // 按账号模糊查询
                .like("userName", userName)                    // 按用户名模糊查询
                .like("userProfile", userProfile)              // 按用户简介模糊查询
                .orderBy(sortField, "ascend".equals(sortOrder)); // 动态排序：true=升序，false=降序
    }


}
