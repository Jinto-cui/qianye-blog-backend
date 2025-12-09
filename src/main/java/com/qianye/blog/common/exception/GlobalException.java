package com.qianye.blog.common.exception;

import com.qianye.blog.common.constant.ErrorCode;
import lombok.Getter;

/**
 * @author: Jinto Cui
 * @desc:  自定义异常类
 * @date: 2025/12/9 23:00
 * @version: v1.0
 */
@Getter
public class GlobalException extends RuntimeException {

    private final int code;

    private final String description;

    public GlobalException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public GlobalException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }


}
