package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章反应（用户级记录，防重复）
 *
 * @author: Jinto Cui
 * @desc: 每用户每文章每种反应一条记录，(post_id, user_id, reaction_type) 联合唯一。
 *        旧表是聚合计数（无 user_id / 无防重），新表支持去重 + 按类型计数。
 *        计数查询：SELECT COUNT(*) FROM post_reaction WHERE post_id=? AND reaction_type=?
 * @date: 2026/05/20
 * @version: v2.0
 * @table: post_reaction
 */
@TableName(value = "post_reaction")
@Data
public class PostReaction implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 文章 ID */
    private Long postId;

    /** 用户 ID（关联 user.id） */
    private Long userId;

    /** 反应类型：clap / heart / fire / thumbs_up */
    private String reactionType;

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
