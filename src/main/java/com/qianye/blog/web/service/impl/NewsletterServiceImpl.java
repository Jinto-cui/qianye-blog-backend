package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.mapper.NewsletterMapper;
import com.qianye.blog.web.model.dto.NewsletterDto;
import com.qianye.blog.web.model.entity.Newsletter;
import com.qianye.blog.web.service.MailService;
import com.qianye.blog.web.service.NewsletterService;
import com.qianye.blog.web.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简报推送服务实现
 *
 * @author: Jinto Cui
 * @desc: 创建即发送，sent_at 记录发送时间；邮件发送由 MailService 占位实现，业务数据保持完整。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@Service
@Slf4j
public class NewsletterServiceImpl extends ServiceImpl<NewsletterMapper, Newsletter>
        implements NewsletterService {

    /** 标题最大长度，对齐 newsletter.subject。 */
    private static final int SUBJECT_MAX_LENGTH = 200;

    /** 正文最大长度，避免异常大请求。 */
    private static final int BODY_MAX_LENGTH = 100000;

    /** 公开历史单页最大条数。 */
    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private MailService mailService;

    @Override
    public List<NewsletterDto> listSent(int limit, int offset) {
        int pageSize = Math.min(Math.max(1, limit), MAX_PAGE_SIZE);
        QueryWrapper<Newsletter> qw = new QueryWrapper<Newsletter>()
                .isNotNull("sent_at")
                .orderByDesc("sent_at")
                .last("limit " + pageSize + " offset " + Math.max(0, offset));
        return list(qw).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public NewsletterDto getSent(Long id) {
        Newsletter newsletter = id == null ? null : getById(id);
        if (newsletter == null || newsletter.getSentAt() == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "简报不存在");
        }
        return toDto(newsletter);
    }

    @Override
    public List<NewsletterDto> listAdmin(int page, int size) {
        int pageSize = Math.min(Math.max(1, size), 100);
        QueryWrapper<Newsletter> qw = new QueryWrapper<Newsletter>()
                .orderByDesc("created_at")
                .last("limit " + pageSize + " offset " + Math.max(0, (page - 1) * pageSize));
        return list(qw).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public long countAdmin() {
        return count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NewsletterDto createAndSend(String subject, String body) {
        String safeSubject = StringUtils.trimToEmpty(subject);
        String safeBody = StringUtils.trimToEmpty(body);
        if (safeSubject.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "简报标题不能为空");
        }
        if (safeSubject.length() > SUBJECT_MAX_LENGTH) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "简报标题最多 200 个字符");
        }
        if (safeBody.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "简报正文不能为空");
        }
        if (safeBody.length() > BODY_MAX_LENGTH) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "简报正文过长");
        }

        Date now = new Date();
        Newsletter newsletter = new Newsletter();
        newsletter.setSubject(safeSubject);
        newsletter.setBody(safeBody);
        newsletter.setSentAt(now);
        newsletter.setCreatedAt(now);
        newsletter.setUpdatedAt(now);
        save(newsletter);

        int recipients = (int) subscriberService.countActive();
        mailService.sendNewsletter(newsletter, recipients);
        log.info("简报已创建并发送, newsletterId={}, recipients={}", newsletter.getId(), recipients);
        return toDto(newsletter);
    }

    @Override
    public void deleteNewsletter(Long id) {
        Newsletter newsletter = id == null ? null : getById(id);
        if (newsletter == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "简报不存在");
        }
        removeById(id);
        log.info("后台删除简报, newsletterId={}", id);
    }

    private NewsletterDto toDto(Newsletter newsletter) {
        NewsletterDto dto = new NewsletterDto();
        dto.setId(newsletter.getId());
        dto.setSubject(newsletter.getSubject());
        dto.setBody(newsletter.getBody());
        dto.setSentAt(newsletter.getSentAt());
        dto.setCreatedAt(newsletter.getCreatedAt());
        return dto;
    }
}
