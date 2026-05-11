package com.qianye.blog.config;

import cn.dev33.satoken.stp.StpInterface;
import com.qianye.blog.web.model.User;
import com.qianye.blog.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限/角色数据源
 * 根据登录用户 ID 返回对应的角色列表和权限列表
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
            if (user != null && user.getRole() != null && user.getRole() == 1) {
                roles.add("admin");
            }
        } catch (Exception ignored) {
        }
        return roles;
    }
}
