package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章
 * 表：bg_post
 */
@TableName(value ="bg_post")
@Data
public class Post implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 外部ID（前端/_id）
     */
    private String extId;

    /**
     * 短链唯一标识
     */
    private String slug;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String description;

    /**
     * 情绪
     */
    private String mood;

    /**
     * 发布时间
     */
    private Date publishedAt;

    /**
     * 阅读分钟数
     */
    private Integer readingTime;

    /**
     * 主图URL
     */
    private String mainImageUrl;

    /**
     * 主图LQIP
     */
    private String mainImageLqip;

    /**
     * 主色前景
     */
    private String mainImageFg;

    /**
     * 主色背景
     */
    private String mainImageBg;

    /**
     * 正文JSON（Portable Text）
     */
    private String bodyJson;

    /**
     * 浏览量
     */
    private Long views;

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