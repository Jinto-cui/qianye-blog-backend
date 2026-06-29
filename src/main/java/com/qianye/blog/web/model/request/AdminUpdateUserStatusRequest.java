package com.qianye.blog.web.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台更新用户状态请求。
 *
 * @author: Jinto Cui
 * @desc: 超级管理员启用或停用普通用户与管理员账号。
 * @date: 2026/06/30 00:51
 * @version: v1.0
 */
@Data
public class AdminUpdateUserStatusRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标状态：0 正常，1 停用 */
    private Integer status;
}
