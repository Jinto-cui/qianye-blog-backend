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
        post.setViews(0L);
        post.setAuthorId(loginUserId);
        post.setPublishedAt(new Date());
        postService.save(post);
        updateCategories(post.getId(), body.get("categoryIds"));
        postAssetService.bindDraftAssets(post.getId(), stringValue(body.get("draftToken")),
                post.getBody(), loginUserId);
        log.info("后台文章创建成功, postId={}, userId={}", post.getId(), loginUserId);
        return toAdminVo(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePost(Long id, Map<String, Object> body, Long loginUserId) {
        Post post = findPost(id);
        fillPost(post, body, false);
        postService.updateById(post);
        updateCategories(id, body.get("categoryIds"));
        postAssetService.syncReferencedAssets(id, stringValue(body.get("draftToken")),
                post.getBody(), loginUserId);
        log.info("后台文章更新成功, postId={}, userId={}", id, loginUserId);
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
        if (create || body.containsKey("title")) post.setTitle((String) body.get("title"));
        if (create || body.containsKey("slug")) post.setSlug((String) body.getOrDefault("slug", ""));
        if (create || body.containsKey("description")) post.setDescription((String) body.get("description"));
        if (create || body.containsKey("body")) post.setBody((String) body.get("body"));
        if (create || body.containsKey("mood")) post.setMood((String) body.getOrDefault("mood", "neutral"));
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
}
