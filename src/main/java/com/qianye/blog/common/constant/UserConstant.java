package com.qianye.blog.common.constant;

/**
 * @author: Jinto Cui
 * @desc: 用户常量
 * @date: 2026/06/30 00:51
 * @version: v1.1
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

    /**
     * 超级管理员
     */
    int SUPER_ADMIN_ROLE = 2;

    /**
     * Sa-Token 管理员角色名
     */
    String ADMIN_ROLE_NAME = "admin";

    /**
     * Sa-Token 超级管理员角色名
     */
    String SUPER_ADMIN_ROLE_NAME = "super_admin";
}
