package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 浏览量
 * 表：post_view
 */
@TableName(value ="post_view")
@Data
public class PostView implements Serializable {
    /**
     * 文章ID
     */
    @TableId
    private Long postId;

    /**
     * 浏览量
     */
    private Long views;

    /**
     * 删除标志（0代表未删除，1代表已删除）
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}