package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.mapper.PostMapper;
import com.qianye.blog.web.model.dto.PostDetailDto;
import com.qianye.blog.web.model.dto.PostDto;
import com.qianye.blog.web.model.entity.*;
import com.qianye.blog.web.model.request.CreateCommentRequest;
import com.qianye.blog.web.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 *
 * @author: Jinto Cui
 * @desc: v2.3 适配新表结构，文章交互接口统一使用 post.id，并限制同一访客短时间重复刷浏览量
 * @date: 2026/06/10 00:42
 * @version: v2.3
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

    /** 浏览量计数窗口：同一文章 + 同一访客 10 分钟最多计 1 次。 */
    private static final long VIEW_WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(10);

    /** 单窗口最大可计数次数。 */
    private static final int VIEW_WINDOW_MAX_COUNT = 1;

    /** 内存级浏览量窗口，后续可替换为 Redis 计数器。 */
    private static final ConcurrentMap<String, ViewWindow> VIEW_WINDOWS = new ConcurrentHashMap<>();

    @Autowired
    private PostReactionService postReactionService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PostCategoryService postCategoryService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private OssClient ossClient;

    @Override
    public List<PostDto> listPosts(int limit, int offset) {
        int pageSize = Math.max(1, limit);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.orderByDesc("published_at");
        qw.last("limit " + pageSize + " offset " + Math.max(0, offset));
        List<Post> posts = list(qw);
        List<PostDto> dtos = posts.stream().map(this::toDto).collect(Collectors.toList());
        log.info("文章列表返回 {} 条", dtos.size());
        return dtos;
    }

    @Override
    public List<String> listSlugs() {
        List<Post> list = list(new QueryWrapper<Post>().select("slug"));
        return list.stream().map(Post::getSlug).collect(Collectors.toList());
    }

    @Override
    public PostDetailDto getPostBySlug(String slug) {
        Post post = getOne(new QueryWrapper<Post>().eq("slug", slug));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }
        return toDetailDto(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long incrViews(Long postId, String visitorKey) {
        Post post = findPostById(postId);
        if (!allowViewIncrement(post.getId(), visitorKey)) {
            Long currentViews = Optional.ofNullable(post.getViews()).orElse(0L);
            log.info("文章浏览量窗口限流, postId={}, visitorKey={}, views={}", postId, maskVisitorKey(visitorKey), currentViews);
            return currentViews;
        }
        boolean updated = lambdaUpdate()
                .eq(Post::getId, post.getId())
                .setSql("views = views + 1")
                .update();
        if (!updated) {
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "浏览量更新失败");
        }
        Long nextViews = Optional.ofNullable(getById(post.getId()).getViews()).orElse(0L);
        log.info("文章浏览量递增, postId={}, views={}", postId, nextViews);
        return nextViews;
    }

    @Override
    public List<Long> batchViews(List<Long> postIds) {
        List<Long> result = new ArrayList<>();
        for (Long postId : postIds) {
            Post post = getById(postId);
            result.add(post == null ? 0L : post.getViews());
        }
        return result;
    }

    @Override
    public List<Integer> getReactions(Long postId) {
        Post post = findPostById(postId);
        QueryWrapper<PostReaction> qw = new QueryWrapper<PostReaction>().eq("post_id", post.getId());
        List<PostReaction> list = postReactionService.list(qw);
        int clap = 0, heart = 0, fire = 0, thumbsUp = 0;
        for (PostReaction r : list) {
            switch (r.getReactionType()) {
                case "clap": clap++; break;
                case "heart": heart++; break;
                case "fire": fire++; break;
                case "thumbs_up": thumbsUp++; break;
            }
        }
        return Arrays.asList(clap, heart, fire, thumbsUp);
    }

    @Override
    public List<Integer> incrReaction(Long postId, int index, long loginId) {
        if (index < 0 || index > 3) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "index 非法");
        }
        Post post = findPostById(postId);
        String reactionType = reactionType(index);
        QueryWrapper<PostReaction> qw = new QueryWrapper<PostReaction>()
                .eq("post_id", post.getId())
                .eq("user_id", loginId)
                .eq("reaction_type", reactionType);
        if (postReactionService.getOne(qw) == null) {
            PostReaction reaction = new PostReaction();
            reaction.setPostId(post.getId());
            reaction.setUserId(loginId);
            reaction.setReactionType(reactionType);
            try {
                postReactionService.save(reaction);
                log.info("文章反应新增, postId={}, userId={}, reactionType={}", postId, loginId, reactionType);
            } catch (DuplicateKeyException e) {
                log.info("文章反应重复提交, postId={}, userId={}, reactionType={}", postId, loginId, reactionType);
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "已经点过这个表情");
            }
        } else {
            log.info("文章反应重复点击, postId={}, userId={}, reactionType={}", postId, loginId, reactionType);
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "已经点过这个表情");
        }
        return getReactions(postId);
    }

    @Override
    public List<Comment> listComments(Long postId) {
        Post post = findPostById(postId);
        QueryWrapper<Comment> qw = new QueryWrapper<Comment>()
                .eq("post_id", post.getId())
                .orderByAsc("created_at");
        return commentService.list(qw);
    }

    @Override
    public Comment addComment(Long postId, CreateCommentRequest req, long loginId) {
        Post post = findPostById(postId);
        if (req.getParentId() != null) {
            Comment parent = commentService.getById(req.getParentId());
            if (parent == null || !parent.getPostId().equals(post.getId())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "父评论不存在");
            }
        }
        Comment c = new Comment();
        c.setPostId(post.getId());
        c.setUserId(loginId);
        c.setBody(req.getBody());
        c.setParentId(req.getParentId());
        commentService.save(c);
        log.info("文章评论新增, postId={}, userId={}, commentId={}", postId, loginId, c.getId());
        return c;
    }

    // ==================== private helpers ====================

    /**
     * 按主键查文章，避免继续依赖旧 Sanity ext_id 字段
     */
    private Post findPostById(Long postId) {
        Post post = getById(postId);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }
        return post;
    }

    private String reactionType(int index) {
        switch (index) {
            case 0:
                return "clap";
            case 1:
                return "heart";
            case 2:
                return "fire";
            case 3:
                return "thumbs_up";
            default:
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "index 非法");
        }
    }

    /**
     * 判断当前访客是否允许为文章增加浏览量。
     */
    private boolean allowViewIncrement(Long postId, String visitorKey) {
        long now = System.currentTimeMillis();
        String safeVisitorKey = visitorKey == null || visitorKey.trim().isEmpty() ? "unknown" : visitorKey.trim();
        String key = postId + ":" + safeVisitorKey;
        ViewWindow window = VIEW_WINDOWS.computeIfAbsent(key, ignored -> new ViewWindow(now));
        synchronized (window) {
            if (now - window.getWindowStartAt() >= VIEW_WINDOW_MILLIS) {
                window.setWindowStartAt(now);
                window.setCount(1);
                cleanupExpiredViewWindows(now);
                return true;
            }
            if (window.getCount() >= VIEW_WINDOW_MAX_COUNT) {
                return false;
            }
            window.setCount(window.getCount() + 1);
            return true;
        }
    }

    /**
     * 简单清理过期窗口，避免长期运行时内存无限增长。
     */
    private void cleanupExpiredViewWindows(long now) {
        if (VIEW_WINDOWS.size() < 10000) {
            return;
        }
        VIEW_WINDOWS.entrySet().removeIf(entry -> now - entry.getValue().getWindowStartAt() >= VIEW_WINDOW_MILLIS);
    }

    private String maskVisitorKey(String visitorKey) {
        if (visitorKey == null || visitorKey.length() <= 12) {
            return visitorKey;
        }
        return visitorKey.substring(0, 12) + "***";
    }

    /**
     * 浏览量内存窗口记录。
     */
    private static class ViewWindow {
        private long windowStartAt;
        private int count;

        ViewWindow(long windowStartAt) {
            this.windowStartAt = windowStartAt;
            this.count = 0;
        }

        long getWindowStartAt() {
            return windowStartAt;
        }

        void setWindowStartAt(long windowStartAt) {
            this.windowStartAt = windowStartAt;
        }

        int getCount() {
            return count;
        }

        void setCount(int count) {
            this.count = count;
        }
    }

    private PostDto toDto(Post post) {
        PostDto dto = new PostDto();
        dto.set_id(String.valueOf(post.getId()));
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        // 组装 mainImage（OSS key → 签名 URL）
        PostDto.MainImage mi = new PostDto.MainImage();
        mi.set_ref(post.getMainImageKey());
        PostDto.Asset asset = new PostDto.Asset();
        asset.setUrl(post.getMainImageKey() != null
                ? ossClient.getAccessUrl(post.getMainImageKey(), 3600) : null);
        asset.setLqip(post.getMainImageLqip());
        PostDto.Dominant dom = new PostDto.Dominant();
        dom.setBackground(post.getMainImageDominantBg());
        dom.setForeground(post.getMainImageDominantFg());
        asset.setDominant(dom);
        mi.setAsset(asset);
        dto.setMainImage(mi);
        dto.setPublishedAt(post.getPublishedAt() != null
                ? post.getPublishedAt().toInstant().toString() : null);
        dto.setDescription(post.getDescription());
        dto.setCategories(getCategoryNames(post.getId()));
        dto.setReadingTime(post.getReadingTime());
        dto.setMood(post.getMood());
        return dto;
    }

    private PostDetailDto toDetailDto(Post post) {
        PostDetailDto dto = new PostDetailDto();
        PostDto base = toDto(post);
        dto.set_id(base.get_id());
        dto.setTitle(base.getTitle());
        dto.setSlug(base.getSlug());
        dto.setMainImage(base.getMainImage());
        dto.setPublishedAt(base.getPublishedAt());
        dto.setDescription(base.getDescription());
        dto.setCategories(base.getCategories());
        dto.setReadingTime(base.getReadingTime());
        dto.setMood(base.getMood());
        dto.setBody(post.getBody());        // v2.0: Markdown 字符串直接返回
        dto.setHeadings(parseHeadings(post.getBody()));
        dto.setRelated(getRelatedPosts(post));
        return dto;
    }

    private List<String> getCategoryNames(Long postId) {
        List<PostCategory> rels = postCategoryService.list(
                new QueryWrapper<PostCategory>().eq("post_id", postId));
        if (rels.isEmpty()) return new ArrayList<>();
        List<Long> catIds = rels.stream()
                .map(PostCategory::getCategoryId).collect(Collectors.toList());
        List<Category> cats = categoryService.list(
                new QueryWrapper<Category>().in("id", catIds));
        return cats.stream().map(Category::getName).collect(Collectors.toList());
    }

    private List<PostDto> getRelatedPosts(Post post) {
        List<PostDto> related = new ArrayList<>();
        List<PostCategory> rels = postCategoryService.list(
                new QueryWrapper<PostCategory>().eq("post_id", post.getId()));
        List<Long> catIds = rels.stream()
                .map(PostCategory::getCategoryId).collect(Collectors.toList());
        if (catIds.isEmpty()) return related;
        List<Long> candidatePostIds = postCategoryService.list(
                new QueryWrapper<PostCategory>().in("category_id", catIds))
                .stream().map(PostCategory::getPostId)
                .distinct().filter(id -> !id.equals(post.getId()))
                .collect(Collectors.toList());
        if (candidatePostIds.isEmpty()) return related;
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.in("id", candidatePostIds).orderByDesc("published_at").last("limit 4");
        return list(qw).stream().map(this::toDto).collect(Collectors.toList());
    }

    private List<Object> parseHeadings(String markdown) {
        List<Object> headings = new ArrayList<>();
        if (markdown == null || markdown.isEmpty()) return headings;
        for (String line : markdown.split("\\n")) {
            line = line.trim();
            if (line.startsWith("## ")) {
                String text = line.substring(3).trim();
                String id = text.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fff]+", "-").replaceAll("^-|-$", "");
                Map<String, String> h = new LinkedHashMap<>();
                h.put("style", "h2");
                h.put("text", text);
                h.put("id", id);
                headings.add(h);
            } else if (line.startsWith("### ")) {
                String text = line.substring(4).trim();
                String id = text.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fff]+", "-").replaceAll("^-|-$", "");
                Map<String, String> h = new LinkedHashMap<>();
                h.put("style", "h3");
                h.put("text", text);
                h.put("id", id);
                headings.add(h);
            }
        }
        return headings;
    }
}
