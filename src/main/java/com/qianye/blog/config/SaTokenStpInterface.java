package com.qianye.blog.config;

import cn.dev33.satoken.stp.StpInterface;
import com.qianye.blog.common.constant.UserConstant;
import com.qianye.blog.web.model.entity.User;
import com.qianye.blog.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色数据源。
 *
 * @author: Jinto Cui
 * @desc: 根据 user.role 映射 admin 与 super_admin，超级管理员继承管理员能力。
 * @date: 2026/06/30 00:51
 * @version: v1.1
 */
@Component
public class SaTokenStpInterface implements StpInterface {

    @Autowired
    private UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        try {
            long userId = Long.parseLong(loginId.toString());
            User user = userService.getById(userId);
            if (user != null && user.getRole() != null) {
                if (user.getRole() == UserConstant.ADMIN_ROLE
                        || user.getRole() == UserConstant.SUPER_ADMIN_ROLE) {
                    roles.add(UserConstant.ADMIN_ROLE_NAME);
                }
                if (user.getRole() == UserConstant.SUPER_ADMIN_ROLE) {
                    roles.add(UserConstant.SUPER_ADMIN_ROLE_NAME);
                }
            }
        } catch (Exception ignored) {
        }
        return roles;
    }
}
