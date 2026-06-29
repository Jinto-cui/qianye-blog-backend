package com.qianye.blog.web.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台更新用户角色请求。
 *
 * @author: Jinto Cui
 * @desc: 仅允许超级管理员把目标用户设为普通用户或管理员，不开放超级管理员授予入口。
 * @date: 2026/06/30 00:51
 * @version: v1.0
 */
@Data
public class AdminUpdateUserRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标角色：0 普通用户，1 管理员 */
    private Integer role;
}
