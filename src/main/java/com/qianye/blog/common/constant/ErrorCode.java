package com.qianye.blog.common.constant;

import lombok.Getter;

/**
 * @author: Jinto Cui
 * @desc: 状态码枚举类
 * @date: 2025/12/9 23:00
 * @version: v1.0
 */
@Getter
public enum ErrorCode {

    SUCCESS(20000, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "用户未登录", ""),
    NO_AUTH(40101, "无当前操作权限", ""),
    SYSTEM_ERROR(50000, "未知异常，请联系管理员！", "");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
