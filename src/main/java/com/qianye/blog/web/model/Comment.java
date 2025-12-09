package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论
 * 表：bg_comment
 */
@TableName(value ="bg_comment")
@Data
public class Comment implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户信息JSON
     */
    private String userInfo;

    /**
     * 评论内容JSON
     */
    private String body;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 删除标志（0代表未删除，1代表已删除）
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}