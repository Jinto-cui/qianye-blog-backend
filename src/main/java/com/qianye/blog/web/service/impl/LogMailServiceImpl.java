package com.qianye.blog.web.service.impl;

import com.qianye.blog.web.model.entity.Newsletter;
import com.qianye.blog.web.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 邮件发送日志占位实现
 *
 * @author: Jinto Cui
 * @desc: 当前阶段不接入第三方邮件服务，确认链接和群发数量写入日志，便于本地联调与后续替换。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@Service
@Slf4j
public class LogMailServiceImpl implements MailService {

    @Override
    public void sendSubscriptionConfirm(String email, String confirmUrl) {
        // 不在日志中输出完整邮箱，保留前后各 2 位便于定位
        log.info("订阅确认邮件（占位）, email={}, confirmUrl={}", maskEmail(email), confirmUrl);
    }

    @Override
    public void sendNewsletter(Newsletter newsletter, int recipientCount) {
        log.info("简报群发（占位）, newsletterId={}, subject={}, recipients={}",
                newsletter.getId(), newsletter.getSubject(), recipientCount);
    }

    private String maskEmail(String email) {
        if (email == null || email.length() < 5) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        String name = email.substring(0, at);
        String domain = email.substring(at);
        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.substring(0, 2) + "***" + domain;
    }
}
