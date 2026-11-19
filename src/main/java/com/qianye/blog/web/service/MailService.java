package com.qianye.blog.web.service;

import com.qianye.blog.web.model.entity.Newsletter;

/**
 * 邮件发送服务
 *
 * @author: Jinto Cui
 * @desc: 初版使用日志占位实现，后续接入 Resend / SMTP 时替换实现即可，不影响业务层。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
public interface MailService {

    /**
     * 发送订阅确认邮件
     *
     * @param email      收件邮箱
     * @param confirmUrl 确认链接
     */
    void sendSubscriptionConfirm(String email, String confirmUrl);

    /**
     * 向已确认订阅者群发简报
     *
     * @param newsletter   简报内容
     * @param recipientCount 已确认订阅者数量
     */
    void sendNewsletter(Newsletter newsletter, int recipientCount);
}
