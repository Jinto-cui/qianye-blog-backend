package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 管理后台评论 DTO。
 *
 * @author: Jinto Cui
 * @desc: 聚合评论、文章和用户展示信息，供后台评论列表审核与定位使用。
 * @date: 2026/06/30 00:31
 * @version: v1.0
 */
@Data
public class AdminCommentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论 ID */
    private Long id;

    /** 文章 ID */
    private Long postId;

    /** 文章标题 */
    private String postTitle;

    /** 文章 slug */
    private String postSlug;

    /** 评论用户 ID */
    private Long userId;

    /** 评论用户展示信息 */
    private CommentUserDto userInfo;

    /** 评论正文 */
    private String body;

    /** 父评论 ID，空表示一级评论 */
    private Long parentId;

    /** 父评论摘要，用于后台识别回复上下文 */
    private String parentBody;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}
