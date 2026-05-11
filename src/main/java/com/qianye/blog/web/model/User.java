package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "user")
@Data
public class User implements Serializable {

    @TableId
    private Long id;

    /** 登录账号 */
    private String userAccount;

    /** 密码（BCrypt） */
    private String userPassword;

    /** 昵称 / 展示名 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像 URL */
    private String avatarUrl;

    /** 个人简介 */
    private String bio;

    /** 社交链接 JSON */
    private String socialLinks;

    /** 角色：0 = 普通用户，1 = 管理员 */
    private Integer role;

    /** 状态：0 = 正常，1 = 停用 */
    private Integer status;

    /** 最后登录时间 */
    private Date lastLoginAt;

    /** 最后登录 IP */
    private String lastLoginIp;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
