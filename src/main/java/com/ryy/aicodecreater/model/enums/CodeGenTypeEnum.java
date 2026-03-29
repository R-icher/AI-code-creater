package com.ryy.aicodecreater.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 代码生成类型枚举
 *
 * 作用：
 * 1. 统一定义系统支持的代码生成模式
 * 2. 避免在业务代码中直接写 "html"、"multi_file" 这样的硬编码字符串
 * 3. 提供根据 value 反查枚举的方法，方便接收前端参数或请求参数后进行业务判断
 */
@Getter
public enum CodeGenTypeEnum {

    /**
     * 原生 HTML 模式
     * 一般表示只生成一个 HTML 文件，例如 index.html
     */
    HTML("原生 HTML 模式", "html"),

    /**
     * 原生多文件模式
     * 一般表示生成多个文件，例如 index.html、style.css、script.js
     */
    MULTI_FILE("原生多文件模式", "multi_file");

    /**
     * 给用户展示的文本说明
     * 例如：原生 HTML 模式、原生多文件模式
     */
    private final String text;

    /**
     * 枚举对应的业务值
     * 一般用于前后端传值、数据库存储、业务判断
     * 例如：html、multi_file
     */
    private final String value;

    /**
     * 枚举构造方法
     *
     * @param text  展示文本
     * @param value 业务值
     */
    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取对应的枚举对象
     *
     * 使用场景：
     * 当前端传来字符串 "html" 或 "multi_file" 时，
     * 可以调用该方法转成对应的枚举值，便于后续统一处理。
     *
     * @param value 枚举的业务值
     * @return 匹配到的枚举对象；如果 value 为空或未匹配到，则返回 null
     */
    public static CodeGenTypeEnum getEnumByValue(String value) {
        // 如果传入值为空，直接返回 null
        if (ObjUtil.isEmpty(value)) {
            return null;
        }

        // 遍历所有枚举值，查找 value 相同的枚举对象
        for (CodeGenTypeEnum anEnum : CodeGenTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }

        // 没有匹配到时返回 null
        return null;
    }
}
