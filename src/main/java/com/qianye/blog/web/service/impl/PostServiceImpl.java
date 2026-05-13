package com.qianye.blog.web.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.mapper.PostMapper;
import com.qianye.blog.web.model.*;
import com.qianye.blog.web.model.dto.PostDetailDto;
import com.qianye.blog.web.model.dto.PostDto;
import com.qianye.blog.web.model.request.CreateCommentRequest;
import com.qianye.blog.web.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

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
    @Autowired
    private UserService userService;

    @Override
    public List<PostDto> listPosts(int limit, int offset) {
        int pageSize = Math.max(1, limit);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.orderByDesc("published_at");
        qw.last("limit " + pageSize + " offset " + Math.max(0, offset));
        List<Post> posts = list(qw);
        List<PostDto> dtos = posts.stream().map(this::toDto).collect(Collectors.toList());
        log.info("请求文章列表返回：{}", dtos);
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
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        return toDetailDto(post);
    }

    @Override
    public Long incrViews(String extId) {
        Post post = findPostByExtId(extId);
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
        post.setViews(pv.getViews());
        updateById(post);
        return pv.getViews();
    }

    @Override
    public List<Long> batchViews(List<String> extIds) {
        List<Long> result = new ArrayList<>();
        for (String extId : extIds) {
            Post post = getOne(new QueryWrapper<Post>().eq("ext_id", extId));
            if (post == null) {
                result.add(0L);
                continue;
            }
            PostView pv = postViewService.getById(post.getId());
            result.add(pv == null ? 0L : pv.getViews());
        }
        return result;
    }

    @Override
    public List<Integer> getReactions(String extId) {
        Post post = findPostByExtId(extId);
        PostReaction pr = postReactionService.getById(post.getId());
        return Arrays.asList(
                pr == null ? 0 : pr.getClap(),
                pr == null ? 0 : pr.getHeart(),
                pr == null ? 0 : pr.getFire(),
                pr == null ? 0 : pr.getThumbsUp()
        );
    }

    @Override
    public List<Integer> incrReaction(String extId, int index) {
        if (index < 0 || index > 3) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "index非法");
        }
        Post post = findPostByExtId(extId);
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
        return Arrays.asList(pr.getClap(), pr.getHeart(), pr.getFire(), pr.getThumbsUp());
    }

    @Override
    public List<Comment> listComments(String extId) {
        Post post = findPostByExtId(extId);
        QueryWrapper<Comment> qw = new QueryWrapper<Comment>()
                .eq("post_id", post.getId())
                .orderByAsc("created_at");
        return commentService.list(qw);
    }

    @Override
    public Comment addComment(String extId, CreateCommentRequest req, long loginId) {
        Post post = findPostByExtId(extId);
        if (req.getParentId() != null) {
            Comment parent = commentService.getById(req.getParentId());
            if (parent == null || !parent.getPostId().equals(post.getId())) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "父评论不存在");
            }
        }
        Comment c = new Comment();
        c.setPostId(post.getId());
        User loginUser = userService.getById(loginId);
        c.setUserId(loginUser != null ? loginUser.getUserAccount() : String.valueOf(loginId));
        c.setUserInfo(req.getUserInfo());
        c.setBody(req.getBody());
        c.setParentId(req.getParentId());
        commentService.save(c);
        return c;
    }

    // ==================== private helpers ====================

    private Post findPostByExtId(String extId) {
        Post post = getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "未找到");
        }
        return post;
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
            candidates = list(qw);
        } else {
            QueryWrapper<Post> qw = new QueryWrapper<>();
            qw.ne("id", post.getId()).orderByDesc("published_at").last("limit 4");
            candidates = list(qw);
        }
        for (Post p : candidates) {
            related.add(toDto(p));
        }
        return related;
    }
}
