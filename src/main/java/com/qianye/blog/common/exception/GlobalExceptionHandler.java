package com.qianye.blog.common.exception;

import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author: Jinto Cui
 * @desc: 全局异常处理器
 * @date: 2025/12/9 23:00
 * @version: v1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GlobalException.class) //指定方法要捕获的异常
    public Result<?> globalExceptionHandler(GlobalException exception) {
        log.error("系统抛出自定义异常！" + exception.getMessage (), exception);
        return ResultUtils.error(exception);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
