package com.qianye.blog.web.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文章正文资源。
 *
 * @author: Jinto Cui
 * @desc: 保存后台编辑器上传的正文图片 OSS key、草稿 token、文章归属和公开状态
 * @date: 2026/06/06 11:32
 * @version: v1.0
 * @table: post_asset
 */
@TableName(value = "post_asset")
@Data
public class PostAsset implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 文章 ID，新建草稿阶段为空 */
    private Long postId;

    /** 前端草稿 token，用于新文章保存前绑定资源 */
    private String draftToken;

    /** OSS object key */
    private String objectKey;

    /** 原始文件名 */
    private String originalName;

    /** MIME 类型 */
    private String mimeType;

    /** 文件大小，单位字节 */
    private Long fileSize;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    /** 图片替代文本 */
    private String alt;

    /** 用途：body 正文图片 */
    private String usageType;

    /** 状态：draft / active / unused / deleted */
    private String status;

    /** 上传人用户 ID */
    private Long createdBy;

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
