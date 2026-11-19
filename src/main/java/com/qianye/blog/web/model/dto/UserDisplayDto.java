package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户展示信息
 *
 * @author: Jinto Cui
 * @desc: 评论、留言等公开内容只暴露头像和展示名，避免前端拿到用户实体中的敏感或无关字段。
 * @date: 2026/11/19 22:10
 * @version: v1.0
 */
@Data
public class UserDisplayDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 展示昵称 */
    private String nickname;

    /** 头像完整 URL */
    private String avatarUrl;
}
