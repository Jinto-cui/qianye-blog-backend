package com.qianye.blog.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理业务异常 + Sa-Token 鉴权异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(GlobalException.class)
    public Result<?> globalExceptionHandler(GlobalException exception) {
        log.error("业务异常: {}", exception.getMessage(), exception);
        return ResultUtils.error(exception);
    }

    @ResponseBody
    @ExceptionHandler(NotLoginException.class)
    public Result<?> notLoginExceptionHandler(NotLoginException e) {
        log.warn("未登录访问受限资源: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NOT_LOGIN, "请先登录");
    }

    @ResponseBody
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public Result<?> notPermissionExceptionHandler(Exception e) {
        log.warn("无权限访问: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.NO_AUTH, "无权访问");
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Result<?> runtimeExceptionHandler(Exception e) {
        log.error("未知异常:", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "");
    }
}
