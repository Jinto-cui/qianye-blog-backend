package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 站点设置
 * 表：site_setting
 */
@TableName(value ="site_setting")
@Data
public class SiteSetting implements Serializable {
    /**
     * 固定ID=1
     */
    @TableId
    private Integer id;

    /**
     * 项目JSON
     */
    private String projects;

    /**
     * 首页图片JSON
     */
    private String heroPhotos;

    /**
     * 简历JSON
     */
    private String resume;

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