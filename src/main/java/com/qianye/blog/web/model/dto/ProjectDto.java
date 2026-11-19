package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 项目展示响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 公开项目列表只返回展示字段，图标 OSS key 转换为签名 URL。
 * @date: 2026/11/19 22:30
 * @version: v1.0
 */
@Data
public class ProjectDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目 ID */
    private Long id;

    /** 项目名称 */
    private String name;

    /** 项目链接 */
    private String url;

    /** 项目简介 */
    private String description;

    /** 图标签名 URL */
    private String iconUrl;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;
}
