package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.constant.UserConstant;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.dto.AdminUserDto;
import com.qianye.blog.web.model.entity.User;
import com.qianye.blog.web.service.AdminUserService;
import com.qianye.blog.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;

/**
 * 超级管理员用户管理服务实现。
 *
 * @author: Jinto Cui
 * @desc: 统一处理用户筛选、角色变更、状态变更和逻辑删除的安全边界。
 * @date: 2026/06/30 00:51
 * @version: v1.0
 */
@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    /** 后台用户每页最大数量。 */
    private static final int MAX_PAGE_SIZE = 50;

    /** 正常状态。 */
    private static final int STATUS_ACTIVE = 0;

    /** 停用状态。 */
    private static final int STATUS_DISABLED = 1;

    @Autowired
    private UserService userService;

    @Override
    public Page<AdminUserDto> listUsers(int page, int size, String keyword, Integer role,
                                        Integer status, Long loginUserId) {
        Page<User> userPage = userService.page(
                new Page<>(normalizePage(page), normalizeSize(size)),
                buildQuery(keyword, role, status));
        Page<AdminUserDto> dtoPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        dtoPage.setRecords(userPage.getRecords().stream()
                .map(user -> toDto(user, loginUserId))
                .collect(Collectors.toList()));
        log.info("后台用户列表返回, page={}, size={}, keywordPresent={}, role={}, status={}, count={}",
                page, size, StringUtils.isNotBlank(keyword), role, status, dtoPage.getRecords().size());
        return dtoPage;
    }

    @Override
    public Long countUsers(String keyword, Integer role, Integer status) {
        return userService.count(buildQuery(keyword, role, status));
    }

    @Override
    public AdminUserDto updateRole(Long id, Integer role, Long loginUserId) {
        User user = findManageableUser(id, loginUserId);
        if (role == null || (role != UserConstant.DEFAULT__ROLE && role != UserConstant.ADMIN_ROLE)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "角色只能设置为普通用户或管理员");
        }
        user.setRole(role);
        user.setUpdatedAt(new Date());
        userService.updateById(user);
        log.info("超级管理员更新用户角色, operatorId={}, userId={}, role={}", loginUserId, id, role);
        return toDto(user, loginUserId);
    }

    @Override
    public AdminUserDto updateStatus(Long id, Integer status, Long loginUserId) {
        User user = findManageableUser(id, loginUserId);
        if (status == null || (status != STATUS_ACTIVE && status != STATUS_DISABLED)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "状态只能设置为正常或停用");
        }
        user.setStatus(status);
        user.setUpdatedAt(new Date());
        userService.updateById(user);
        log.info("超级管理员更新用户状态, operatorId={}, userId={}, status={}", loginUserId, id, status);
        return toDto(user, loginUserId);
    }

    @Override
    public void deleteUser(Long id, Long loginUserId) {
        User user = findManageableUser(id, loginUserId);
        userService.removeById(user.getId());
        log.info("超级管理员删除用户, operatorId={}, userId={}", loginUserId, id);
    }

    private QueryWrapper<User> buildQuery(String keyword, Integer role, Integer status) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        String trimmedKeyword = StringUtils.trimToNull(keyword);
        if (trimmedKeyword != null) {
            qw.and(wrapper -> wrapper.like("user_account", trimmedKeyword)
                    .or().like("nickname", trimmedKeyword)
                    .or().like("email", trimmedKeyword));
        }
        if (role != null) {
            validateRoleFilter(role);
            qw.eq("role", role);
        }
        if (status != null) {
            validateStatusFilter(status);
            qw.eq("status", status);
        }
        qw.orderByDesc("created_at");
        return qw;
    }

    private User findManageableUser(Long id, Long loginUserId) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户 ID 非法");
        }
        if (id.equals(loginUserId)) {
            throw new GlobalException(ErrorCode.NO_AUTH, "不能操作自己的账号");
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        if (user.getRole() != null && user.getRole() == UserConstant.SUPER_ADMIN_ROLE) {
            throw new GlobalException(ErrorCode.NO_AUTH, "不能操作超级管理员账号");
        }
        return user;
    }

    private void validateRoleFilter(Integer role) {
        if (role < UserConstant.DEFAULT__ROLE || role > UserConstant.SUPER_ADMIN_ROLE) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "角色筛选条件非法");
        }
    }

    private void validateStatusFilter(Integer status) {
        if (status != STATUS_ACTIVE && status != STATUS_DISABLED) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "状态筛选条件非法");
        }
    }

    private AdminUserDto toDto(User user, Long loginUserId) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUserAccount(user.getUserAccount());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setSelf(user.getId() != null && user.getId().equals(loginUserId));
        return dto;
    }

    private int normalizePage(int page) {
        return Math.max(1, page);
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(MAX_PAGE_SIZE, size));
    }
}
