package com.ryy.aicodecreater.aop;

import com.ryy.aicodecreater.annotation.AuthCheck;
import com.ryy.aicodecreater.exception.BusinessException;
import com.ryy.aicodecreater.exception.ErrorCode;
import com.ryy.aicodecreater.model.entity.User;
import com.ryy.aicodecreater.model.enums.UserRoleEnum;
import com.ryy.aicodecreater.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限拦截器
 *
 * 基于 AOP + 自定义注解 @AuthCheck 实现统一的权限校验。
 * 在执行目标方法前，先判断当前登录用户是否具备所需角色。
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 用户服务，用于获取当前登录用户信息
     */
    @Resource
    private UserService userService;

    /**
     * 环绕通知：拦截所有带有 @AuthCheck 注解的方法，并进行权限校验
     *
     * @param joinPoint 切入点，表示当前被拦截的方法
     * @param authCheck 权限校验注解，可获取注解中声明的必须角色
     * @return 目标方法执行结果
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {

        // 获取注解中定义的必须具备的角色
        String mustRole = authCheck.mustRole();

        // 获取当前请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

        // 从请求上下文中取出 HttpServletRequest 对象
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 将注解中的角色字符串转换为对应的枚举值
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 如果注解未指定角色，或指定角色无效，则默认不需要权限，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // =========================
        // 以下逻辑表示：必须具备指定权限才允许访问
        // =========================

        // 获取当前登录用户的角色枚举
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());

        // 如果当前用户角色为空或无效，说明没有权限，抛出无权限异常
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 如果接口要求管理员权限，但当前用户不是管理员，则拒绝访问
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 权限校验通过，继续执行目标方法
        return joinPoint.proceed();
    }
}
