package com.qianye.blog.common.constant;

/**
 * @author: Jinto Cui
 * @desc: 用户常量
 * @date: 2025/12/9 23:00
 * @version: v1.0
 */
public interface UserConstant {
    /**
     * 用户登录态 键
     */
    String USER_LOGIN_STATUS = "userLoginStatus";

    // ------------权限-------------
    /**
     * 普通用户
     */
    int DEFAULT__ROLE = 0;
    /**
     * 管理员
     */
    int ADMIN_ROLE = 1;
}
