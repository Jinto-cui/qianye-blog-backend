package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章评论响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 对齐前端评论区展示字段，隐藏逻辑删除标识等内部字段。
 * @date: 2026/06/17 17:30
 * @version: v1.0
 */
@Data
public class CommentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论 ID */
    private Long id;

    /** 文章 ID */
    private Long postId;

    /** 评论用户 ID */
    private Long userId;

    /** 评论正文，支持安全 Markdown 子集 */
    private String body;

    /** 父评论 ID，空表【示一级评论 */
    private Long parentId;

    /** 用户展示信息 */
    private CommentUserDto userInfo;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
