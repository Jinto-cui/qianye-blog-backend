package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Comment;
import com.qianye.blog.web.model.Post;
import com.qianye.blog.web.model.PostReaction;
import com.qianye.blog.web.model.PostView;
import com.qianye.blog.web.model.dto.PostDetailDto;
import com.qianye.blog.web.model.dto.PostDto;
import com.qianye.blog.web.model.request.CreateCommentRequest;
import com.qianye.blog.web.service.CommentService;
import com.qianye.blog.web.service.PostReactionService;
import com.qianye.blog.web.service.PostService;
import com.qianye.blog.web.service.PostViewService;
import com.qianye.blog.utils.ResultUtils;
import com.qianye.blog.web.model.*;
import com.qianye.blog.web.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章接口
 * 提供文章的增删改查与分页查询
 */
@RestController
@RequestMapping("/rest/v1")
@Slf4j
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private PostViewService postViewService;
    @Autowired
    private PostReactionService postReactionService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private PostCategoryService postCategoryService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取文章列表（limit/offset）
     */
    @GetMapping("/posts")
    public Result<List<PostDto>> listPosts(@RequestParam(required = false, defaultValue = "5") Integer limit,
                                           @RequestParam(required = false, defaultValue = "0") Integer offset) {
        int pageSize = Math.max(1, limit);
        int pageNum = offset < 0 ? 1 : (offset / pageSize) + 1;
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.orderByDesc("published_at");
        // 使用 limit/offset 语义
        qw.last("limit " + pageSize + " offset " + Math.max(0, offset));
        List<Post> posts = postService.list(qw);
        List<PostDto> dtos = posts.stream().map(this::toDto).collect(Collectors.toList());
        log.info("请求文章列表返回：{}", dtos);
        return ResultUtils.success(dtos);
    }

    /**
     * 获取所有文章 slug
     */
    @GetMapping("/posts/slugs")
    public Result<List<String>> listSlugs() {
        List<Post> list = postService.list(new QueryWrapper<Post>().select("slug"));
        List<String> slugs = list.stream().map(Post::getSlug).collect(Collectors.toList());
        return ResultUtils.success(slugs);
    }

    /**
     * 获取文章详情（按 slug）
     */
    @GetMapping("/posts/{slug}")
    public Result<PostDetailDto> getPostBySlug(@PathVariable String slug) {
        Post post = postService.getOne(new QueryWrapper<Post>().eq("slug", slug));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        PostDetailDto dto = toDetailDto(post);
        return ResultUtils.success(dto);
    }

    /**
     * 递增浏览量并返回最新值（ext_id）
     */
    @PostMapping("/posts/{id}/views/incr")
    public Result<Long> incrViews(@PathVariable("id") String extId) {
        Post post = postService.getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        PostView pv = postViewService.getById(post.getId());
        if (pv == null) {
            pv = new PostView();
            pv.setPostId(post.getId());
            pv.setViews(1L);
            postViewService.save(pv);
        } else {
            pv.setViews(pv.getViews() + 1);
            postViewService.updateById(pv);
        }
        // 同步 bg_post.views
        post.setViews(pv.getViews());
        postService.updateById(post);
        return ResultUtils.success(pv.getViews());
    }

    private PostDto toDto(Post post) {
        PostDto dto = new PostDto();
        dto.set_id(post.getExtId());
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        PostDto.MainImage mi = new PostDto.MainImage();
        mi.set_ref(post.getExtId());
        PostDto.Asset asset = new PostDto.Asset();
        String url = post.getMainImageUrl();
        if (url == null || (url.startsWith("http") && url.contains("cdn.example.com"))) {
            String slug = post.getSlug();
            if ("hello-world".equals(slug)) {
                url = "https://images.unsplash.com/photo-1529070538774-1843cb3265df?w=1200&q=80&auto=format&fit=crop";
            } else if ("java-tips-2025".equals(slug)) {
                url = "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=1200&q=80&auto=format&fit=crop";
            } else if ("portable-text-intro".equals(slug)) {
                url = "https://images.unsplash.com/photo-1517511620798-cec17d428bc0?w=1200&q=80&auto=format&fit=crop";
            }
        }
        asset.setUrl(url);
        asset.setLqip(post.getMainImageLqip());
        PostDto.Dominant dom = new PostDto.Dominant();
        dom.setBackground(post.getMainImageBg());
        dom.setForeground(post.getMainImageFg());
        asset.setDominant(dom);
        mi.setAsset(asset);
        dto.setMainImage(mi);
        dto.setPublishedAt(post.getPublishedAt() != null ? post.getPublishedAt().toInstant().toString() : null);
        dto.setDescription(post.getDescription());
        dto.setCategories(getCategoryTitles(post.getId()));
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
        ObjectMapper mapper = new ObjectMapper();
        Object body = null;
        try {
            if (post.getBodyJson() != null && !post.getBodyJson().isEmpty()) {
                body = mapper.readValue(post.getBodyJson(), Object.class);
            }
        } catch (Exception ignored) {}
        dto.setBody(body);
        dto.setHeadings(new ArrayList<>());
        dto.setRelated(getRelatedPosts(post));
        return dto;
    }

    private List<String> getCategoryTitles(Long postId) {
        List<PostCategory> rels = postCategoryService.list(new QueryWrapper<PostCategory>().eq("post_id", postId));
        if (rels.isEmpty()) return new ArrayList<>();
        List<Long> catIds = rels.stream().map(PostCategory::getCategoryId).collect(Collectors.toList());
        List<Category> cats = categoryService.list(new QueryWrapper<Category>().in("id", catIds));
        return cats.stream().map(Category::getTitle).collect(Collectors.toList());
    }

    private List<PostDto> getRelatedPosts(Post post) {
        List<PostDto> related = new ArrayList<>();
        List<PostCategory> rels = postCategoryService.list(new QueryWrapper<PostCategory>().eq("post_id", post.getId()));
        List<Long> catIds = rels.stream().map(PostCategory::getCategoryId).collect(Collectors.toList());
        List<Long> candidatePostIds = new ArrayList<>();
        if (!catIds.isEmpty()) {
            List<PostCategory> others = postCategoryService.list(new QueryWrapper<PostCategory>().in("category_id", catIds));
            candidatePostIds = others.stream().map(PostCategory::getPostId).distinct().filter(id -> !id.equals(post.getId())).collect(Collectors.toList());
        }
        List<Post> candidates;
        if (!candidatePostIds.isEmpty()) {
            QueryWrapper<Post> qw = new QueryWrapper<>();
            qw.in("id", candidatePostIds).orderByDesc("published_at").last("limit 4");
            candidates = postService.list(qw);
        } else {
            QueryWrapper<Post> qw = new QueryWrapper<>();
            qw.ne("id", post.getId()).orderByDesc("published_at").last("limit 4");
            candidates = postService.list(qw);
        }
        for (Post p : candidates) {
            related.add(toDto(p));
        }
        return related;
    }

    /**
     * 批量查询浏览量（ext_id 列表）
     */
    @GetMapping("/posts/views")
    public Result<List<Long>> batchViews(@RequestParam("ids") String ids) {
        List<String> extIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<Long> result = new ArrayList<>();
        for (String extId : extIds) {
            Post post = postService.getOne(new QueryWrapper<Post>().eq("ext_id", extId));
            if (post == null) {
                result.add(0L);
                continue;
            }
            PostView pv = postViewService.getById(post.getId());
            result.add(pv == null ? 0L : pv.getViews());
        }
        return ResultUtils.success(result);
    }

    /**
     * 获取文章表情计数（顺序：[clap, heart, fire, thumbsUp]）
     */
    @GetMapping("/posts/{id}/reactions")
    public Result<List<Integer>> getReactions(@PathVariable("id") String extId) {
        Post post = postService.getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        PostReaction pr = postReactionService.getById(post.getId());
        List<Integer> arr = Arrays.asList(
                pr == null ? 0 : pr.getClap(),
                pr == null ? 0 : pr.getHeart(),
                pr == null ? 0 : pr.getFire(),
                pr == null ? 0 : pr.getThumbsUp()
        );
        return ResultUtils.success(arr);
    }

    /**
     * 指定下标计数自增（0..3）
     */
    @PatchMapping("/posts/{id}/reactions")
    public Result<List<Integer>> incrReaction(@PathVariable("id") String extId,
                                              @RequestParam("index") Integer index) {
        if (index == null || index < 0 || index > 3) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "index非法");
        }
        Post post = postService.getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        PostReaction pr = postReactionService.getById(post.getId());
        if (pr == null) {
            pr = new PostReaction();
            pr.setPostId(post.getId());
            pr.setClap(0);
            pr.setHeart(0);
            pr.setFire(0);
            pr.setThumbsUp(0);
            postReactionService.save(pr);
        }
        switch (index) {
            case 0: pr.setClap((pr.getClap() == null ? 0 : pr.getClap()) + 1); break;
            case 1: pr.setHeart((pr.getHeart() == null ? 0 : pr.getHeart()) + 1); break;
            case 2: pr.setFire((pr.getFire() == null ? 0 : pr.getFire()) + 1); break;
            case 3: pr.setThumbsUp((pr.getThumbsUp() == null ? 0 : pr.getThumbsUp()) + 1); break;
        }
        postReactionService.updateById(pr);
        List<Integer> arr = Arrays.asList(pr.getClap(), pr.getHeart(), pr.getFire(), pr.getThumbsUp());
        return ResultUtils.success(arr);
    }

    /**
     * 获取文章评论列表（升序，ext_id）
     */
    @GetMapping("/posts/{id}/comments")
    public Result<List<Comment>> listComments(@PathVariable("id") String extId) {
        Post post = postService.getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        QueryWrapper<Comment> qw = new QueryWrapper<Comment>()
                .eq("post_id", post.getId())
                .orderByAsc("created_at");
        return ResultUtils.success(commentService.list(qw));
    }

    /**
     * 新增评论（需鉴权，ext_id）
     */
    @PostMapping("/posts/{id}/comments")
    public Result<Comment> addComment(@PathVariable("id") String extId,
                                      @RequestBody CreateCommentRequest req,
                                      HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute("userLoginStatus");
        if (userObj == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN, "未认证");
        }
        Post post = postService.getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        if (req.getParentId() != null) {
            Comment parent = commentService.getById(req.getParentId());
            if (parent == null || !parent.getPostId().equals(post.getId())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "父评论不存在");
            }
        }
        Comment c = new Comment();
        c.setPostId(post.getId());
        c.setUserId(((User) userObj).getUserAccount());
        c.setUserInfo(req.getUserInfo());
        c.setBody(req.getBody());
        c.setParentId(req.getParentId());
        commentService.save(c);
        return ResultUtils.success(c);
    }
}