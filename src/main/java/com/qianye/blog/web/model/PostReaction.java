package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 表情计数
 * 表：post_reaction
 */
@TableName(value ="post_reaction")
@Data
public class PostReaction implements Serializable {
    /**
     * 文章ID
     */
    @TableId
    private Long postId;

    /**
     * 👏
     */
    private Integer clap;

    /**
     * ❤️
     */
    private Integer heart;

    /**
     * 🔥
     */
    private Integer fire;

    /**
     * 👍
     */
    private Integer thumbsUp;

    /**
     * 删除标志（0代表未删除，1代表已删除）
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}