package com.qianye.blog.web.service;

import com.qianye.blog.web.model.dto.ContentSafetyResult;

/**
 * 内容安全检测服务
 *
 * @author: Jinto Cui
 * @desc: 评论、留言等用户生成内容统一从这里接入本地词表和规则检测。
 * @date: 2026/06/17 23:40
 * @version: v1.0
 */
public interface ContentSafetyService {

    /**
     * 检测评论内容是否适合公开发布
     */
    ContentSafetyResult checkComment(String content);
}
