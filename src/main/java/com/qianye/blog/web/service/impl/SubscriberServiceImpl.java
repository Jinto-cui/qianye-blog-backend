package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.mapper.SubscriberMapper;
import com.qianye.blog.web.model.dto.SubscriberDto;
import com.qianye.blog.web.model.entity.Subscriber;
import com.qianye.blog.web.service.MailService;
import com.qianye.blog.web.service.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 简报订阅服务实现
 *
 * @author: Jinto Cui
 * @desc: 双确认订阅 + 退订 + 后台查询；确认邮件当前由 MailService 占位实现发送。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@Service
@Slf4j
public class SubscriberServiceImpl extends ServiceImpl<SubscriberMapper, Subscriber>
        implements SubscriberService {

    /** 邮箱格式校验，拒绝明显非法输入。 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** 邮箱最大长度，对齐 subscriber.email 字段。 */
    private static final int EMAIL_MAX_LENGTH = 128;

    /** 后台分页单页最大条数。 */
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private MailService mailService;

    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void subscribe(String email) {
        String normalized = normalizeEmail(email);
        Subscriber existing = getOne(new QueryWrapper<Subscriber>().eq("email", normalized));
        Date now = new Date();

        if (existing != null && Integer.valueOf(1).equals(existing.getConfirmed())
                && existing.getUnsubscribedAt() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "该邮箱已经订阅过简报");
        }

        String token = newToken();
        if (existing != null) {
            existing.setToken(token);
            existing.setConfirmed(0);
            existing.setUnsubscribedAt(null);
            existing.setUpdatedAt(now);
            updateById(existing);
        } else {
            Subscriber subscriber = new Subscriber();
            subscriber.setEmail(normalized);
            subscriber.setToken(token);
            subscriber.setConfirmed(0);
            subscriber.setCreatedAt(now);
            subscriber.setUpdatedAt(now);
            save(subscriber);
        }

        mailService.sendSubscriptionConfirm(normalized, buildConfirmUrl(token));
        log.info("订阅请求已受理, email={}, exists={}", maskEmail(normalized), existing != null);
    }

    @Override
    public String confirm(String token) {
        Subscriber subscriber = findByToken(token);
        if (Integer.valueOf(1).equals(subscriber.getConfirmed()) && subscriber.getUnsubscribedAt() == null) {
            return maskEmail(subscriber.getEmail());
        }
        Date now = new Date();
        subscriber.setConfirmed(1);
        if (subscriber.getSubscribedAt() == null) {
            subscriber.setSubscribedAt(now);
        }
        subscriber.setUnsubscribedAt(null);
        subscriber.setUpdatedAt(now);
        updateById(subscriber);
        log.info("订阅已确认, subscriberId={}", subscriber.getId());
        return maskEmail(subscriber.getEmail());
    }

    @Override
    public String unsubscribe(String token) {
        Subscriber subscriber = findByToken(token);
        if (subscriber.getUnsubscribedAt() != null) {
            return maskEmail(subscriber.getEmail());
        }
        Date now = new Date();
        subscriber.setConfirmed(0);
        subscriber.setUnsubscribedAt(now);
        subscriber.setUpdatedAt(now);
        updateById(subscriber);
        log.info("订阅已退订, subscriberId={}", subscriber.getId());
        return maskEmail(subscriber.getEmail());
    }

    @Override
    public long countActive() {
        return count(new QueryWrapper<Subscriber>()
                .eq("confirmed", 1)
                .isNull("unsubscribed_at"));
    }

    @Override
    public List<SubscriberDto> listSubscribers(int page, int size, String keyword) {
        int pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        QueryWrapper<Subscriber> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            qw.like("email", keyword.trim());
        }
        qw.orderByDesc("created_at");
        qw.last("limit " + pageSize + " offset " + Math.max(0, (page - 1) * pageSize));
        return list(qw).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public long countSubscribers(String keyword) {
        QueryWrapper<Subscriber> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            qw.like("email", keyword.trim());
        }
        return count(qw);
    }

    @Override
    public void deleteSubscriber(Long id) {
        Subscriber subscriber = id == null ? null : getById(id);
        if (subscriber == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "订阅者不存在");
        }
        removeById(id);
        log.info("后台删除订阅者, subscriberId={}", id);
    }

    private Subscriber findByToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "确认链接无效");
        }
        Subscriber subscriber = getOne(new QueryWrapper<Subscriber>().eq("token", token.trim()));
        if (subscriber == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "确认链接无效或已过期");
        }
        return subscriber;
    }

    private String normalizeEmail(String email) {
        String normalized = StringUtils.trimToEmpty(email).toLowerCase();
        if (normalized.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (normalized.length() > EMAIL_MAX_LENGTH) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "邮箱长度不能超过 128 位");
        }
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        return normalized;
    }

    private String newToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildConfirmUrl(String token) {
        String base = StringUtils.removeEnd(frontendBaseUrl, "/");
        return base + "/confirm/" + token;
    }

    private String maskEmail(String email) {
        if (StringUtils.isBlank(email) || email.indexOf('@') <= 0) {
            return "***";
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String domain = email.substring(at);
        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.substring(0, 2) + "***" + domain;
    }

    private SubscriberDto toDto(Subscriber subscriber) {
        SubscriberDto dto = new SubscriberDto();
        dto.setId(subscriber.getId());
        dto.setEmail(subscriber.getEmail());
        dto.setConfirmed(subscriber.getConfirmed());
        dto.setSubscribedAt(subscriber.getSubscribedAt());
        dto.setUnsubscribedAt(subscriber.getUnsubscribedAt());
        dto.setCreatedAt(subscriber.getCreatedAt());
        return dto;
    }
}
