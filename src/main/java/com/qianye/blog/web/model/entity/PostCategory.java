package com.qianye.blog.web.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章-分类关联（多对多）
 *
 * @author: Jinto Cui
 * @desc: 关联表，UNIQUE(post_id, category_id) 防止重复关联
 * @date: 2026/05/19
 * @version: v2.0
 * @table: post_category
 */
@TableName(value = "post_category")
@Data
public class PostCategory implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 文章 ID */
    private Long postId;

    /** 分类 ID */
    private Long categoryId;

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
