package com.ryy.aicodecreater.exception;

import com.ryy.aicodecreater.common.BaseResponse;
import com.ryy.aicodecreater.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常的自定义异常处理器
     * 当系统中抛出BusinessException类型的异常时，此方法会被自动调用
     *
     * @param e 捕获到的BusinessException异常对象
     * @return 返回一个BaseResponse对象，包含错误码和错误信息
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        // 记录异常的堆栈信息到日志中，方便后续排查问题
        log.error("BusinessException", e);
        // 使用Result工具类构建错误响应，包含异常的错误码和错误信息
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 全局异常处理类
     * 用于捕获和处理系统运行时出现的异常
     *
     * @ExceptionHandler 注解用于捕获指定类型的异常
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        // 记录异常日志，便于后续排查问题
        // 返回系统错误信息，ErrorCode.SYSTEM_ERROR 表示系统错误码
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
