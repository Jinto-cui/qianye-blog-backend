package com.qianye.blog.web.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 简报推送记录
 *
 * @author: Jinto Cui
 * @desc: 每次推送的历史记录。旧表名 newsletters（复数），修正为单数 newsletter。
 *        body 存 HTML 或 Markdown，由邮件模板渲染后发送。
 * @date: 2026/05/20
 * @version: v2.0
 * @table: newsletter
 */
@TableName(value = "newsletter")
@Data
public class Newsletter implements Serializable {

    /** 主键 */
    @TableId
    private Long id;

    /** 邮件标题 */
    private String subject;

    /** 邮件正文（HTML 或 Markdown） */
    private String body;

    /** 实际发送时间（NULL 表示草稿/未发送） */
    private Date sentAt;

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
