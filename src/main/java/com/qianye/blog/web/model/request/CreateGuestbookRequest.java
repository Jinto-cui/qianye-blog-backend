package com.qianye.blog.web.model.request;

import lombok.Data;

/**
 * 新增留言请求
 *
 * @author: Jinto Cui
 * @desc: body 为 Markdown 子集文本，用户信息完全由后端按登录态关联生成。
 * @date: 2026/11/19 22:10
 * @version: v1.0
 */
@Data
public class CreateGuestbookRequest {

    /** 留言正文 */
    private String body;
}
