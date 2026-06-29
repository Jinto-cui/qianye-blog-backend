package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.web.model.dto.AdminUserDto;

/**
 * 超级管理员用户管理服务。
 *
 * @author: Jinto Cui
 * @desc: 仅超级管理员可调用，用于管理普通用户和管理员账号。
 * @date: 2026/06/30 00:51
 * @version: v1.0
 */
public interface AdminUserService {

    /**
     * 分页查询用户。
     */
    Page<AdminUserDto> listUsers(int page, int size, String keyword, Integer role,
                                 Integer status, Long loginUserId);

    /**
     * 统计用户数量。
     */
    Long countUsers(String keyword, Integer role, Integer status);

    /**
     * 更新用户角色。
     */
    AdminUserDto updateRole(Long id, Integer role, Long loginUserId);

    /**
     * 更新用户状态。
     */
    AdminUserDto updateStatus(Long id, Integer status, Long loginUserId);

    /**
     * 删除用户。
     */
    void deleteUser(Long id, Long loginUserId);
}
