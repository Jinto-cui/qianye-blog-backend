package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 简报推送响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 公开历史只返回已发送简报，后台列表可看到草稿/发送时间。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@Data
public class NewsletterDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 简报 ID */
    private Long id;

    /** 邮件标题 */
    private String subject;

    /** 正文 Markdown */
    private String body;

    /** 实际发送时间，NULL 表示草稿 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sentAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
