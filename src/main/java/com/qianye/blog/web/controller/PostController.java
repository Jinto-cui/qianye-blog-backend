package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.utils.IPUtils;
import com.qianye.blog.web.model.entity.Comment;
import com.qianye.blog.web.model.dto.PostDetailDto;
import com.qianye.blog.web.model.dto.PostDto;
import com.qianye.blog.web.model.request.CreateCommentRequest;
import com.qianye.blog.web.service.PostService;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章公开接口
 *
 * @author: Jinto Cui
 * @desc: 提供文章列表、详情、浏览量、反应和评论接口，所有文章 ID 均使用 post.id
 * @date: 2026/06/10 00:32
 * @version: v2.2
 */
@RestController
@RequestMapping("/rest/v1")
@Slf4j
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 获取文章列表（limit/offset）
     */
    @GetMapping("/posts")
    public Result<List<PostDto>> listPosts(@RequestParam(required = false, defaultValue = "5") Integer limit,
                                           @RequestParam(required = false, defaultValue = "0") Integer offset) {
        return ResultUtils.success(postService.listPosts(limit, offset));
    }

    /**
     * 获取所有文章 slug
     */
    @GetMapping("/posts/slugs")
    public Result<List<String>> listSlugs() {
        return ResultUtils.success(postService.listSlugs());
    }

    /**
     * 获取文章详情（按 slug）
     */
    @GetMapping("/posts/{slug}")
    public Result<PostDetailDto> getPostBySlug(@PathVariable String slug) {
        return ResultUtils.success(postService.getPostBySlug(slug));
    }

    /**
     * 递增浏览量并返回最新值
     */
    @PostMapping("/posts/{id}/views/incr")
    public Result<Long> incrViews(@PathVariable("id") Long postId, HttpServletRequest request) {
        return ResultUtils.success(postService.incrViews(postId, buildViewVisitorKey(request)));
    }

    /**
     * 批量查询浏览量（post.id 列表）
     */
    @GetMapping("/posts/views")
    public Result<List<Long>> batchViews(@RequestParam("ids") String ids) {
        List<Long> postIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(this::parsePostId)
                .collect(Collectors.toList());
        return ResultUtils.success(postService.batchViews(postIds));
    }

    /**
     * 获取文章表情计数（顺序：[clap, heart, fire, thumbsUp]）
     */
    @GetMapping("/posts/{id}/reactions")
    public Result<List<Integer>> getReactions(@PathVariable("id") Long postId) {
        return ResultUtils.success(postService.getReactions(postId));
    }

    /**
     * 指定下标计数自增（0..3）
     */
    @SaCheckLogin
    @PatchMapping("/posts/{id}/reactions")
    public Result<List<Integer>> incrReaction(@PathVariable("id") Long postId,
                                              @RequestParam("index") Integer index) {
        if (index == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "index非法");
        }
        long loginId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(postService.incrReaction(postId, index, loginId));
    }

    /**
     * 获取文章评论列表（升序）
     */
    @GetMapping("/posts/{id}/comments")
    public Result<List<Comment>> listComments(@PathVariable("id") Long postId) {
        return ResultUtils.success(postService.listComments(postId));
    }

    /**
     * 新增评论（需鉴权）
     */
    @SaCheckLogin
    @PostMapping("/posts/{id}/comments")
    public Result<Comment> addComment(@PathVariable("id") Long postId,
                                      @RequestBody CreateCommentRequest req) {
        long loginId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(postService.addComment(postId, req, loginId));
    }

    private Long parsePostId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "文章ID非法");
        }
    }

    /**
     * 构造浏览量限流访客标识：登录用户优先按用户限制，匿名访问按客户端 IP 限制。
     */
    private String buildViewVisitorKey(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            return "user:" + StpUtil.getLoginIdAsLong();
        }
        String ip = IPUtils.getIp(request);
        if (StringUtils.isBlank(ip)) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + (StringUtils.isBlank(ip) ? "unknown" : ip);
    }
}
