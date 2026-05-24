package com.qianye.blog.web.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目展示
 *
 * @author: Jinto Cui
 * @desc: 独立表管理项目列表，替代旧 site_setting.projects JSON 嵌套，支持独立 CRUD 和排序
 * @date: 2026/05/19
 * @version: v1.0
 * @table: project
 */
@TableName(value = "project")
@Data
public class Project implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 项目名称 */
    private String name;

    /** 项目链接 */
    private String url;

    /** 项目简介 */
    private String description;

    /** 图标 OSS object key（完整 URL = oss_base_url + icon_key） */
    private String iconKey;

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
