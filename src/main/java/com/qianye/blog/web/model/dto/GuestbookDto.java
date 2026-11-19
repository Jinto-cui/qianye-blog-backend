package com.qianye.blog.web.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 留言墙响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 对齐前端留言墙展示字段，用户信息由后端按 user_id 关联生成，不信任前端传入。
 * @date: 2026/11/19 22:10
 * @version: v1.0
 */
@Data
public class GuestbookDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 留言 ID */
    private Long id;

    /** 留言用户 ID */
    private Long userId;

    /** 留言正文，支持安全 Markdown 子集 */
    private String body;

    /** 用户展示信息 */
    private UserDisplayDto userInfo;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
