package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章
 *
 * @author: Jinto Cui
 * @desc: 博客文章实体，正文存 Markdown，主图字段扁平化，不再依赖 Sanity Portable Text
 * @date: 2026/05/19
 * @version: v2.0
 * @table: post
 */
@TableName(value = "post")
@Data
public class Post implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 文章标题 */
    private String title;

    /** URL 标识符，用于路由 /posts/{slug} */
    private String slug;

    /** 摘要（列表页展示） */
    private String description;

    /** 正文（Markdown 格式，前端 unified/remark 渲染） */
    private String body;

    /** 情绪：neutral / happy / sad */
    private String mood;

    /** 发布时间（NULL 表示草稿） */
    private Date publishedAt;

    /** 阅读时长（分钟） */
    private Integer readingTime;

    /** 主图 OSS object key（完整 URL = oss_base_url + main_image_key） */
    private String mainImageKey;

    /** 主图 LQIP（base64 缩略图，前端 blur-up 加载效果） */
    private String mainImageLqip;

    /** 主图主色背景（#RRGGBB） */
    private String mainImageDominantBg;

    /** 主图主色前景（#RRGGBB） */
    private String mainImageDominantFg;

    /** 浏览量 */
    private Long views;

    /** 作者 ID（关联 user.id） */
    private Long authorId;

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
