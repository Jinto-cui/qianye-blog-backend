package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.model.dto.AdminCommentDto;
import com.qianye.blog.web.model.dto.CommentUserDto;
import com.qianye.blog.web.model.entity.Comment;
import com.qianye.blog.web.model.entity.Post;
import com.qianye.blog.web.model.entity.User;
import com.qianye.blog.web.service.AdminCommentService;
import com.qianye.blog.web.service.CommentService;
import com.qianye.blog.web.service.PostService;
import com.qianye.blog.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理后台评论服务实现。
 *
 * @author: Jinto Cui
 * @desc: 后台评论查询聚合文章和用户展示字段，删除使用 MyBatis-Plus 逻辑删除。
 * @date: 2026/06/30 00:31
 * @version: v1.0
 */
@Service
@Slf4j
public class AdminCommentServiceImpl implements AdminCommentService {

    /** 后台评论每页最大数量，避免一次拉取过多数据。 */
    private static final int MAX_PAGE_SIZE = 50;

    /** 父评论摘要最大长度。 */
    private static final int PARENT_BODY_SUMMARY_LENGTH = 80;

    @Autowired
    private CommentService commentService;
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private OssClient ossClient;

    @Override
    public Page<AdminCommentDto> listComments(int page, int size, Long postId, String keyword) {
        Page<Comment> commentPage = commentService.page(
                new Page<>(normalizePage(page), normalizeSize(size)),
                buildQuery(postId, keyword));
        List<Comment> comments = commentPage.getRecords();
        Map<Long, Post> postMap = loadPosts(comments);
        Map<Long, User> userMap = loadUsers(comments);
        Map<Long, Comment> parentMap = loadParents(comments);
        Page<AdminCommentDto> dtoPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        dtoPage.setRecords(comments.stream()
                .map(comment -> toDto(comment, postMap.get(comment.getPostId()),
                        userMap.get(comment.getUserId()), parentMap.get(comment.getParentId())))
                .collect(Collectors.toList()));
        log.info("后台评论列表返回, page={}, size={}, postId={}, keywordPresent={}, count={}",
                page, size, postId, StringUtils.isNotBlank(keyword), dtoPage.getRecords().size());
        return dtoPage;
    }

    @Override
    public Long countComments(Long postId, String keyword) {
        return commentService.count(buildQuery(postId, keyword));
    }

    @Override
    public void deleteComment(Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "评论 ID 非法");
        }
        Comment comment = commentService.getById(id);
        if (comment == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "评论不存在");
        }
        commentService.removeById(id);
        log.info("后台评论删除成功, commentId={}, postId={}, userId={}",
                id, comment.getPostId(), comment.getUserId());
    }

    private QueryWrapper<Comment> buildQuery(Long postId, String keyword) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        if (postId != null) {
            if (postId <= 0) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章 ID 非法");
            }
            qw.eq("post_id", postId);
        }
        String trimmedKeyword = StringUtils.trimToNull(keyword);
        if (trimmedKeyword != null) {
            List<Long> matchedPostIds = findPostIdsByTitle(trimmedKeyword);
            qw.and(wrapper -> {
                wrapper.like("body", trimmedKeyword);
                if (!matchedPostIds.isEmpty()) {
                    wrapper.or().in("post_id", matchedPostIds);
                }
            });
        }
        qw.orderByDesc("created_at");
        return qw;
    }

    private List<Long> findPostIdsByTitle(String keyword) {
        return postService.list(new QueryWrapper<Post>().like("title", keyword))
                .stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<Long, Post> loadPosts(List<Comment> comments) {
        List<Long> postIds = comments.stream()
                .map(Comment::getPostId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postService.list(new QueryWrapper<Post>().in("id", postIds))
                .stream()
                .collect(Collectors.toMap(Post::getId, post -> post, (left, right) -> left));
    }

    private Map<Long, User> loadUsers(List<Comment> comments) {
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
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

    private Map<Long, Comment> loadParents(List<Comment> comments) {
        Set<Long> parentIds = comments.stream()
                .map(Comment::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentService.list(new QueryWrapper<Comment>().in("id", parentIds))
                .stream()
                .collect(Collectors.toMap(Comment::getId, comment -> comment, (left, right) -> left));
    }

    private AdminCommentDto toDto(Comment comment, Post post, User user, Comment parent) {
        AdminCommentDto dto = new AdminCommentDto();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPostId());
        dto.setPostTitle(post == null ? "已删除文章" : post.getTitle());
        dto.setPostSlug(post == null ? null : post.getSlug());
        dto.setUserId(comment.getUserId());
        dto.setUserInfo(toUserDto(comment.getUserId(), user));
        dto.setBody(comment.getBody());
        dto.setParentId(comment.getParentId());
        dto.setParentBody(parent == null ? null : summarize(parent.getBody()));
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }

    private CommentUserDto toUserDto(Long userId, User user) {
        CommentUserDto dto = new CommentUserDto();
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

    private String summarize(String body) {
        if (StringUtils.isBlank(body)) {
            return "";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= PARENT_BODY_SUMMARY_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, PARENT_BODY_SUMMARY_LENGTH) + "...";
    }

    private int normalizePage(int page) {
        return Math.max(1, page);
    }

    private int normalizeSize(int size) {
        return Math.max(1, Math.min(MAX_PAGE_SIZE, size));
    }
}
