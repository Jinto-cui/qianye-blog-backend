package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论用户展示信息
 *
 * @author: Jinto Cui
 * @desc: 评论区只暴露头像和展示名，避免前端拿到用户实体中的敏感或无关字段。
 * @date: 2026/06/17 17:30
 * @version: v1.0
 */
@Data
public class CommentUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    private Long id;

    /** 展示昵称 */
    private String nickname;

    /** 头像完整 URL */
    private String avatarUrl;
}
