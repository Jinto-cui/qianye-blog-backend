package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.mapper.GuestbookMapper;
import com.qianye.blog.web.model.dto.ContentSafetyResult;
import com.qianye.blog.web.model.dto.GuestbookDto;
import com.qianye.blog.web.model.dto.UserDisplayDto;
import com.qianye.blog.web.model.entity.Guestbook;
import com.qianye.blog.web.model.entity.User;
import com.qianye.blog.web.service.ContentSafetyService;
import com.qianye.blog.web.service.GuestbookService;
import com.qianye.blog.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 留言墙服务实现
 *
 * @author: Jinto Cui
 * @desc: 留言用户信息统一按 user_id 关联生成；写入前经过内容安全和 60 秒频率窗口校验。
 * @date: 2026/11/19 22:10
 * @version: v1.0
 */
@Service
@Slf4j
public class GuestbookServiceImpl extends ServiceImpl<GuestbookMapper, Guestbook>
        implements GuestbookService {

    /** 留言正文最大长度，对齐 template 留言墙输入上限。 */
    private static final int MESSAGE_MAX_LENGTH = 600;

    /** 单页最多返回条数，防止一次拉取过多数据。 */
    private static final int MAX_PAGE_SIZE = 50;

    /** 同一用户两次留言的最小间隔。 */
    private static final long MESSAGE_WINDOW_MILLIS = TimeUnit.SECONDS.toMillis(60);

    @Autowired
    private UserService userService;

    @Autowired
    private OssClient ossClient;

    @Autowired
    private ContentSafetyService contentSafetyService;

    @Override
    public List<GuestbookDto> listMessages(int limit, int offset) {
        int pageSize = Math.min(Math.max(1, limit), MAX_PAGE_SIZE);
        int safeOffset = Math.max(0, offset);
        QueryWrapper<Guestbook> qw = new QueryWrapper<Guestbook>()
                .orderByDesc("created_at")
                .last("limit " + pageSize + " offset " + safeOffset);
        List<Guestbook> messages = list(qw);
        Map<Long, User> userMap = loadMessageUsers(messages);
        List<GuestbookDto> dtos = messages.stream()
                .map(message -> toDto(message, userMap.get(message.getUserId())))
                .collect(Collectors.toList());
        log.info("留言列表返回, limit={}, offset={}, count={}", pageSize, safeOffset, dtos.size());
        return dtos;
    }

    @Override
    public GuestbookDto addMessage(long loginId, String body) {
        if (StringUtils.isBlank(body)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "留言内容不能为空");
        }
        String content = body.trim();
        if (content.length() > MESSAGE_MAX_LENGTH) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "留言最多 600 个字符");
        }
        ContentSafetyResult safetyResult = contentSafetyService.checkComment(content);
        if (!safetyResult.isPassed()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, safetyResult.getMessage());
        }
        checkMessageFrequency(loginId);

        Guestbook message = new Guestbook();
        message.setUserId(loginId);
        message.setBody(content);
        message.setCreatedAt(new Date());
        message.setUpdatedAt(message.getCreatedAt());
        save(message);
        log.info("留言新增, userId={}, guestbookId={}", loginId, message.getId());
        return toDto(message, userService.getById(loginId));
    }

    /**
     * 校验同一用户的留言频率，避免短时间内刷屏。
     */
    private void checkMessageFrequency(long loginId) {
        Guestbook last = getOne(new QueryWrapper<Guestbook>()
                .eq("user_id", loginId)
                .orderByDesc("created_at")
                .last("limit 1"));
        if (last == null || last.getCreatedAt() == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - last.getCreatedAt().getTime();
        if (elapsed < MESSAGE_WINDOW_MILLIS) {
            long waitSeconds = Math.max(1, (MESSAGE_WINDOW_MILLIS - elapsed) / 1000);
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "留言过于频繁，请 " + waitSeconds + " 秒后再试");
        }
    }

    /**
     * 批量加载留言用户，避免 N+1 查询。
     */
    private Map<Long, User> loadMessageUsers(List<Guestbook> messages) {
        List<Long> userIds = messages.stream()
                .map(Guestbook::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.list(new QueryWrapper<User>().in("id", userIds))
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));
    }

    private GuestbookDto toDto(Guestbook message, User user) {
        GuestbookDto dto = new GuestbookDto();
        dto.setId(message.getId());
        dto.setUserId(message.getUserId());
        dto.setBody(message.getBody());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUserInfo(toUserDisplay(message.getUserId(), user));
        return dto;
    }

    private UserDisplayDto toUserDisplay(Long userId, User user) {
        UserDisplayDto dto = new UserDisplayDto();
        dto.setId(userId);
        if (user == null) {
            dto.setNickname("已注销用户");
            dto.setAvatarUrl(null);
            return dto;
        }
        dto.setNickname(StringUtils.defaultIfBlank(user.getNickname(), user.getUserAccount()));
        dto.setAvatarUrl(StringUtils.isNotBlank(user.getAvatarKey())
                ? ossClient.getAccessUrl(user.getAvatarKey(), 3600)
                : null);
        return dto;
    }
}
