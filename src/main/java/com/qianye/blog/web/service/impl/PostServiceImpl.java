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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 *
 * @author: Jinto Cui
 * @desc: v2.0 适配新表结构，移除 PostView，views 直接存 post 表
 * @date: 2026/05/22
 * @version: v2.0
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {

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
    public Long incrViews(String extId) {
        // v2.0: views 直接存 post 表
        Post post = findPostByExtId(extId);
        post.setViews(post.getViews() + 1);
        updateById(post);
        return post.getViews();
    }

    @Override
    public List<Long> batchViews(List<String> extIds) {
        List<Long> result = new ArrayList<>();
        for (String extId : extIds) {
            Post post = getOne(new QueryWrapper<Post>().eq("ext_id", extId));
            result.add(post == null ? 0L : post.getViews());
        }
        return result;
    }

    @Override
    public List<Integer> getReactions(String extId) {
        // v2.0: 由 COUNT GROUP BY 查询，此处返回空数组占位
        Post post = findPostByExtId(extId);
        if (post == null) return Arrays.asList(0, 0, 0, 0);
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
    public List<Integer> incrReaction(String extId, int index) {
        if (index < 0 || index > 3) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "index 非法");
        }
        // v2.0: 用户级反应记录，需要 loginId。此处为兼容旧接口仅返回计数
        Post post = findPostByExtId(extId);
        return getReactions(extId);
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
        c.setUserId(loginId);
        c.setBody(req.getBody());
        c.setParentId(req.getParentId());
        commentService.save(c);
        return c;
    }

    // ==================== private helpers ====================

    /**
     * 按 ext_id 查文章（兼容旧接口，ext_id 仅存在于旧数据）
     */
    private Post findPostByExtId(String extId) {
        Post post = getOne(new QueryWrapper<Post>().eq("ext_id", extId));
        if (post == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }
        return post;
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
