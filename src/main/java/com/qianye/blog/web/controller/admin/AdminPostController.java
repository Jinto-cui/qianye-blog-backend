package com.qianye.blog.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.entity.Post;
import com.qianye.blog.web.model.entity.PostCategory;
import com.qianye.blog.web.service.PostCategoryService;
import com.qianye.blog.web.service.PostService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台 — 文章 CRUD
 *
 * @author: Jinto Cui
 * @desc: 文章的增删改查，含分类关联管理
 * @date: 2026/05/25 22:00
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminPostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PostCategoryService postCategoryService;

    /**
     * 文章列表（含草稿），按更新时间降序
     */
    @GetMapping("/posts")
    public Result<List<Map<String, Object>>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.orderByDesc("updated_at");
        int offset = (page - 1) * size;
        qw.last("limit " + size + " offset " + offset);
        List<Post> posts = postService.list(qw);
        List<Map<String, Object>> result = posts.stream().map(this::toAdminVo).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /** 文章总数 */
    @GetMapping("/posts/count")
    public Result<Long> countPosts() {
        return ResultUtils.success(postService.count());
    }

    /** 单篇文章详情（编辑用） */
    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPost(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }
        return ResultUtils.success(toAdminVo(post));
    }

    /** 新建文章 */
    @PostMapping("/posts")
    public Result<Map<String, Object>> createPost(@RequestBody Map<String, Object> body) {
        Post post = new Post();
        post.setTitle((String) body.get("title"));
        post.setSlug((String) body.getOrDefault("slug", ""));
        post.setDescription((String) body.get("description"));
        post.setBody((String) body.get("body"));
        post.setMood((String) body.getOrDefault("mood", "neutral"));
        post.setReadingTime(body.get("readingTime") != null
                ? ((Number) body.get("readingTime")).intValue() : 0);
        post.setMainImageKey((String) body.get("mainImageKey"));
        post.setMainImageLqip((String) body.get("mainImageLqip"));
        post.setMainImageDominantBg((String) body.get("mainImageDominantBg"));
        post.setMainImageDominantFg((String) body.get("mainImageDominantFg"));
        post.setViews(0L);
        post.setPublishedAt(new Date());
        postService.save(post);

        // 关联分类
        updateCategories(post.getId(), body.get("categoryIds"));
        return ResultUtils.success(toAdminVo(post));
    }

    /** 更新文章 */
    @PutMapping("/posts/{id}")
    public Result<Map<String, Object>> updatePost(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body) {
        Post post = postService.getById(id);
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }
        if (body.containsKey("title")) post.setTitle((String) body.get("title"));
        if (body.containsKey("slug")) post.setSlug((String) body.get("slug"));
        if (body.containsKey("description")) post.setDescription((String) body.get("description"));
        if (body.containsKey("body")) post.setBody((String) body.get("body"));
        if (body.containsKey("mood")) post.setMood((String) body.get("mood"));
        if (body.containsKey("readingTime")) post.setReadingTime(((Number) body.get("readingTime")).intValue());
        if (body.containsKey("mainImageKey")) post.setMainImageKey((String) body.get("mainImageKey"));
        if (body.containsKey("mainImageLqip")) post.setMainImageLqip((String) body.get("mainImageLqip"));
        if (body.containsKey("mainImageDominantBg")) post.setMainImageDominantBg((String) body.get("mainImageDominantBg"));
        if (body.containsKey("mainImageDominantFg")) post.setMainImageDominantFg((String) body.get("mainImageDominantFg"));
        if (body.containsKey("publishedAt")) {
            // publishedAt 字段保持原值，暂不支持修改
        }
        postService.updateById(post);
        updateCategories(id, body.get("categoryIds"));
        return ResultUtils.success(toAdminVo(post));
    }

    /** 删除文章（逻辑删除） */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.removeById(id);
        return ResultUtils.success(null);
    }

    // ==================== helpers ====================

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

        // 分类 ID 列表
        List<PostCategory> rels = postCategoryService.list(
                new QueryWrapper<PostCategory>().eq("post_id", post.getId()));
        vo.put("categoryIds", rels.stream().map(PostCategory::getCategoryId).collect(Collectors.toList()));

        return vo;
    }

    private void updateCategories(Long postId, Object categoryIdsObj) {
        // 先删后插
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
}
