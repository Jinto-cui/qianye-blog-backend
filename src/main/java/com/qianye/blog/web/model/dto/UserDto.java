package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息响应（用于登录和当前用户接口）
 *
 * @author: Jinto Cui
 * @desc: 对外隐藏密码等敏感字段，暴露角色和状态用于前端权限判断。
 * @date: 2026/06/30 00:51
 * @version: v1.1
 */
@Data
public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userAccount;
    private String nickname;
    private String email;
    private String avatarUrl;
    private String bio;
    private String socialLinks;
    private Integer role;
    private Integer status;
    private String token;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
