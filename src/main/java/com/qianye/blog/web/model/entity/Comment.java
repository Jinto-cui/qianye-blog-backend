package com.qianye.blog.web.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章评论
 *
 * @author: Jinto Cui
 * @desc: user_id 关联 user 表 JOIN 获取头像昵称，不再冗余存 user_info JSON。
 *        parent_id 支持嵌套回复（单层或递归均可）。
 * @date: 2026/05/20
 * @version: v2.0
 * @table: comment
 */
@TableName(value = "comment")
@Data
public class Comment implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 文章 ID */
    private Long postId;

    /** 评论用户 ID（关联 user.id） */
    private Long userId;

    /** 评论内容（纯文本或 Markdown 片段） */
    private String body;

    /** 父评论 ID（NULL 表示顶级评论） */
    private Long parentId;

    /** 创建时间 */
    private Date createdAt;

    /** 更新时间 */
    private Date updatedAt;

    /** 逻辑删除：0 = 未删除，1 = 已删除 */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
