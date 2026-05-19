package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章分类
 *
 * @author: Jinto Cui
 * @desc: 分类实体，用于文章归类与分类页展示
 * @date: 2026/05/19
 * @version: v2.0
 * @table: category
 */
@TableName(value = "category")
@Data
public class Category implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 分类名称（如：前端 / 后端 / 设计） */
    private String name;

    /** URL 标识符，用于路由 /categories/{slug} */
    private String slug;

    /** 简要描述 */
    private String description;

    /** 排序权重（数值越大越靠前） */
    private Integer sortOrder;

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
