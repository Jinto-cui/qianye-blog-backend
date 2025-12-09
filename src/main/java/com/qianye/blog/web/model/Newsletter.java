package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 简报
 * 表：newsletters
 */
@TableName(value ="newsletters")
@Data
public class Newsletter implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 标题
     */
    private String subject;

    /**
     * 正文
     */
    private String body;

    /**
     * 发送时间
     */
    private Date sentAt;

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