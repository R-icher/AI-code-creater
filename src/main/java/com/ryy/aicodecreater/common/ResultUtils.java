package com.ryy.aicodecreater.common;

import com.ryy.aicodecreater.exception.ErrorCode;

/**
 * 结果工具类，用于生成统一格式的响应结果
 */
public class ResultUtils {

    /**
     * 成功响应方法
     * 用于生成成功的响应结果，包含数据内容
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应，包含状态码0、数据和"ok"消息
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败响应方法1
     * 用于生成失败的响应结果，使用预设的错误码

     *
     * @param errorCode 错误码对象，包含错误码和错误信息
     * @return 响应，包含错误码、null数据和错误信息
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败响应方法2
     * 用于生成失败的响应结果，使用自定义的错误码和错误信息
     * @param code    错误码
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
