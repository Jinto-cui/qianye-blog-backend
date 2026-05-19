package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 站点配置（单行表，固定列）
 *
 * @author: Jinto Cui
 * @desc: id 固定为 1，单行存储所有站点级配置。字段按需增加，ALTER TABLE 可控。
 *        替代旧 site_setting 表（命名不规范、缺少 created_at/updated_at/deleted）。
 * @date: 2026/05/19
 * @version: v2.0
 * @table: site_config
 */
@TableName(value = "site_config")
@Data
public class SiteConfig implements Serializable {

    /** 主键（固定为 1，保证只有一行数据） */
    @TableId
    private Long id;

    /** 首页顶部图片 OSS key 数组 [\"posts/hero/1.jpg\", ...] */
    private String heroPhotos;

    /** 简历经历 [{ \"company\", \"title\", \"logoKey\", \"start\", \"end\" }] */
    private String resume;

    /** 社交链接 [{ \"platform\", \"url\" }] */
    private String socialLinks;

    /** OSS 基础 URL，用于拼接完整图片地址（如 https://oss-cn-hangzhou.aliyuncs.com） */
    private String ossBaseUrl;

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
