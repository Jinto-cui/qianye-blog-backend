package com.qianye.blog.common;

import com.qianye.blog.common.constant.ErrorCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author: Jinto Cui
 * @desc: 通用返回对象
 * @date: 2025/12/9 23:00
 * @version: v1.0
 */
@Getter
public class Result<T> implements Serializable {

    /**
     * 响应状态码
     */
    private final int code;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 数据对象
     */
    private final T data;

    /**
     * 描述
     */
    public String description;

    public Result(int code, String message, String description, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.description = description;
    }

    public Result(int code, String message, T data) {
        this(code, message, "", data);
    }

    public Result(int code, T data) {
        this(code, "", "", data);
    }

    public Result(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), errorCode.getDescription(), null);
    }

}
