package com.qianye.blog.web.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.UserConstant;
import com.qianye.blog.utils.ResultUtils;
import com.qianye.blog.web.model.dto.AdminUserDto;
import com.qianye.blog.web.model.request.AdminUpdateUserRoleRequest;
import com.qianye.blog.web.model.request.AdminUpdateUserStatusRequest;
import com.qianye.blog.web.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超级管理员用户管理接口。
 *
 * @author: Jinto Cui
 * @desc: 只有 super_admin 可以管理普通用户与管理员账号。
 * @date: 2026/06/30 00:51
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin/users")
@SaCheckRole(UserConstant.SUPER_ADMIN_ROLE_NAME)
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 分页查询用户列表。
     */
    @GetMapping
    public Result<Page<AdminUserDto>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {
        return ResultUtils.success(adminUserService.listUsers(
                page, size, keyword, role, status, StpUtil.getLoginIdAsLong()));
    }

    /**
     * 查询用户总数。
     */
    @GetMapping("/count")
    public Result<Long> countUsers(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer role,
                                   @RequestParam(required = false) Integer status) {
        return ResultUtils.success(adminUserService.countUsers(keyword, role, status));
    }

    /**
     * 更新用户角色。
     */
    @PutMapping("/{id}/role")
    public Result<AdminUserDto> updateRole(@PathVariable Long id,
                                           @RequestBody AdminUpdateUserRoleRequest req) {
        return ResultUtils.success(adminUserService.updateRole(
                id, req == null ? null : req.getRole(), StpUtil.getLoginIdAsLong()));
    }

    /**
     * 更新用户状态。
     */
    @PutMapping("/{id}/status")
    public Result<AdminUserDto> updateStatus(@PathVariable Long id,
                                             @RequestBody AdminUpdateUserStatusRequest req) {
        return ResultUtils.success(adminUserService.updateStatus(
                id, req == null ? null : req.getStatus(), StpUtil.getLoginIdAsLong()));
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id, StpUtil.getLoginIdAsLong());
        return ResultUtils.success(null);
    }
}
