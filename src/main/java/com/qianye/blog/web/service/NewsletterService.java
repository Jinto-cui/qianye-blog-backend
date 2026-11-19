package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.dto.NewsletterDto;
import com.qianye.blog.web.model.entity.Newsletter;

import java.util.List;

/**
 * 简报推送服务
 *
 * @author: Jinto Cui
 * @desc: 公开历史只暴露已发送简报；后台负责创建、发送和删除。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
public interface NewsletterService extends IService<Newsletter> {

    /**
     * 公开已发送简报列表
     */
    List<NewsletterDto> listSent(int limit, int offset);

    /**
     * 公开已发送简报详情
     */
    NewsletterDto getSent(Long id);

    /**
     * 后台简报分页
     */
    List<NewsletterDto> listAdmin(int page, int size);

    /**
     * 后台简报数量
     */
    long countAdmin();

    /**
     * 后台创建并立即发送简报
     */
    NewsletterDto createAndSend(String subject, String body);

    /**
     * 后台删除简报
     */
    void deleteNewsletter(Long id);
}
