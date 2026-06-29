package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理后台用户 DTO。
 *
 * @author: Jinto Cui
 * @desc: 超级管理员管理用户时使用的安全展示字段，不返回密码等敏感信息。
 * @date: 2026/06/30 00:51
 * @version: v1.0
 */
@Data
public class AdminUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 登录账号 */
    private String userAccount;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 角色：0 普通用户，1 管理员，2 超级管理员 */
    private Integer role;

    /** 状态：0 正常，1 停用 */
    private Integer status;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginAt;

    /** 最后登录 IP */
    private String lastLoginIp;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    /** 是否当前登录用户 */
    private Boolean self;
}
