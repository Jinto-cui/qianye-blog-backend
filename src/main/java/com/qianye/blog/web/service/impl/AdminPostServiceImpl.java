package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.entity.Post;
import com.qianye.blog.web.model.entity.PostCategory;
import com.qianye.blog.web.service.AdminPostService;
import com.qianye.blog.web.service.PostAssetService;
import com.qianye.blog.web.service.PostCategoryService;
import com.qianye.blog.web.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台文章服务实现。
 *
 * @author: Jinto Cui
 * @desc: 后台文章 CRUD 事务边界，统一维护文章分类和正文资源引用状态
 * @date: 2026/06/06 11:48
 * @version: v1.0
 */
@Service
@Slf4j
public class AdminPostServiceImpl implements AdminPostService {

    /** 草稿状态：仅后台可见。 */
    private static final String STATUS_DRAFT = "draft";

    /** 已发布状态：公开接口可见。 */
    private static final String STATUS_PUBLISHED = "published";

    /** 已下架状态：仅后台可见，保留首次发布时间。 */
    private static final String STATUS_OFFLINE = "offline";

    /** 保存草稿动作。 */
    private static final String ACTION_SAVE_DRAFT = "save_draft";

    /** 发布文章动作。 */
    private static final String ACTION_PUBLISH = "publish";

    /** 普通更新动作，不改变发布状态。 */
    private static final String ACTION_UPDATE = "update";

    /** 下架文章动作。 */
    private static final String ACTION_OFFLINE = "offline";

    @Autowired
    private PostService postService;
    @Autowired
    private PostCategoryService postCategoryService;
    @Autowired
    private PostAssetService postAssetService;

    @Override
    public List<Map<String, Object>> listPosts(int page, int size) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.orderByDesc("updated_at");
        qw.last("limit " + Math.max(1, size) + " offset " + Math.max(0, (page - 1) * size));
        return postService.list(qw).stream().map(this::toAdminVo).collect(Collectors.toList());
    }

    @Override
    public Long countPosts() {
        return postService.count();
    }

    @Override
    public Map<String, Object> getPost(Long id) {
        Post post = findPost(id);
        return toAdminVo(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPost(Map<String, Object> body, Long loginUserId) {
        Post post = new Post();
        fillPost(post, body, true);
        validateRequiredFields(post);
        post.setViews(0L);
        post.setAuthorId(loginUserId);
        applyPublishAction(post, stringValue(body.get("publishAction")), true);
        postService.save(post);
        updateCategories(post.getId(), body.get("categoryIds"));
        postAssetService.bindDraftAssets(post.getId(), stringValue(body.get("draftToken")),
                post.getBody(), loginUserId);
        log.info("后台文章创建成功, postId={}, userId={}, status={}", post.getId(), loginUserId, post.getStatus());
        return toAdminVo(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePost(Long id, Map<String, Object> body, Long loginUserId) {
        Post post = findPost(id);
        fillPost(post, body, false);
        validateRequiredFields(post);
        applyPublishAction(post, stringValue(body.get("publishAction")), false);
        post.setUpdatedAt(new Date());
        postService.updateById(post);
        updateCategories(id, body.get("categoryIds"));
        postAssetService.syncReferencedAssets(id, stringValue(body.get("draftToken")),
                post.getBody(), loginUserId);
        log.info("后台文章更新成功, postId={}, userId={}, status={}", id, loginUserId, post.getStatus());
        return toAdminVo(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        postService.removeById(id);
        log.info("后台文章删除成功, postId={}", id);
    }

    private Post findPost(Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }
        return post;
    }

    private void fillPost(Post post, Map<String, Object> body, boolean create) {
        if (create || body.containsKey("title")) post.setTitle(trimString(body.get("title")));
        if (create || body.containsKey("slug")) post.setSlug(trimString(body.getOrDefault("slug", "")));
        if (create || body.containsKey("description")) post.setDescription((String) body.get("description"));
        if (create || body.containsKey("body")) post.setBody((String) body.get("body"));
        if (create || body.containsKey("mood")) post.setMood((String) body.getOrDefault("mood", "neutral"));
        if (create) post.setStatus(STATUS_DRAFT);
        if (create || body.containsKey("readingTime")) {
            post.setReadingTime(body.get("readingTime") != null
                    ? ((Number) body.get("readingTime")).intValue() : 0);
        }
        if (create || body.containsKey("mainImageKey")) post.setMainImageKey((String) body.get("mainImageKey"));
        if (create || body.containsKey("mainImageLqip")) post.setMainImageLqip((String) body.get("mainImageLqip"));
        if (create || body.containsKey("mainImageDominantBg")) {
            post.setMainImageDominantBg((String) body.get("mainImageDominantBg"));
        }
        if (create || body.containsKey("mainImageDominantFg")) {
            post.setMainImageDominantFg((String) body.get("mainImageDominantFg"));
        }
    }

    private void validateRequiredFields(Post post) {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章标题不能为空");
        }
        if (post.getSlug() == null || post.getSlug().trim().isEmpty()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章 slug 不能为空");
        }
    }

    private void applyPublishAction(Post post, String action, boolean create) {
        String normalizedAction = action == null || action.trim().isEmpty()
                ? (create ? ACTION_PUBLISH : ACTION_UPDATE)
                : action.trim();
        if (ACTION_SAVE_DRAFT.equals(normalizedAction)) {
            post.setStatus(STATUS_DRAFT);
            return;
        }
        if (ACTION_PUBLISH.equals(normalizedAction)) {
            post.setStatus(STATUS_PUBLISHED);
            if (post.getPublishedAt() == null) {
                post.setPublishedAt(new Date());
            }
            return;
        }
        if (ACTION_UPDATE.equals(normalizedAction)) {
            if (post.getStatus() == null || post.getStatus().trim().isEmpty()) {
                post.setStatus(post.getPublishedAt() == null ? STATUS_DRAFT : STATUS_PUBLISHED);
            }
            return;
        }
        if (ACTION_OFFLINE.equals(normalizedAction)) {
            post.setStatus(STATUS_OFFLINE);
            return;
        }
        throw new GlobalException(ErrorCode.PARAMS_ERROR, "publishAction 非法");
    }

    private void updateCategories(Long postId, Object categoryIdsObj) {
        postCategoryService.remove(new QueryWrapper<PostCategory>().eq("post_id", postId));
        if (categoryIdsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) categoryIdsObj;
            for (Number n : ids) {
                PostCategory pc = new PostCategory();
                pc.setPostId(postId);
                pc.setCategoryId(n.longValue());
                postCategoryService.save(pc);
            }
        }
    }

    private Map<String, Object> toAdminVo(Post post) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", post.getId());
        vo.put("title", post.getTitle());
        vo.put("slug", post.getSlug());
        vo.put("description", post.getDescription());
        vo.put("body", post.getBody());
        vo.put("mood", post.getMood());
        vo.put("status", post.getStatus());
        vo.put("readingTime", post.getReadingTime());
        vo.put("mainImageKey", post.getMainImageKey());
        vo.put("mainImageLqip", post.getMainImageLqip());
        vo.put("mainImageDominantBg", post.getMainImageDominantBg());
        vo.put("mainImageDominantFg", post.getMainImageDominantFg());
        vo.put("views", post.getViews());
        vo.put("publishedAt", post.getPublishedAt());
        vo.put("createdAt", post.getCreatedAt());
        vo.put("updatedAt", post.getUpdatedAt());

        List<PostCategory> rels = postCategoryService.list(
                new QueryWrapper<PostCategory>().eq("post_id", post.getId()));
        vo.put("categoryIds", rels.stream().map(PostCategory::getCategoryId).collect(Collectors.toList()));
        return vo;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String trimString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
