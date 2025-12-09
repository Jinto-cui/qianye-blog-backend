package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 文章-分类关系
 * 表：post_category
 */
@TableName(value ="post_category")
@Data
public class PostCategory implements Serializable {
    /**
     * 文章ID
     */
    private Long postId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 删除标志（0代表未删除，1代表已删除）
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}