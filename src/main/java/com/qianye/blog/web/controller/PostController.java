package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Comment;
import com.qianye.blog.web.model.dto.PostDetailDto;
import com.qianye.blog.web.model.dto.PostDto;
import com.qianye.blog.web.model.request.CreateCommentRequest;
import com.qianye.blog.web.service.PostService;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     * 递增浏览量并返回最新值（ext_id）
     */
    @PostMapping("/posts/{id}/views/incr")
    public Result<Long> incrViews(@PathVariable("id") String extId) {
        return ResultUtils.success(postService.incrViews(extId));
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
        return ResultUtils.success(postService.batchViews(extIds));
    }

    /**
     * 获取文章表情计数（顺序：[clap, heart, fire, thumbsUp]）
     */
    @GetMapping("/posts/{id}/reactions")
    public Result<List<Integer>> getReactions(@PathVariable("id") String extId) {
        return ResultUtils.success(postService.getReactions(extId));
    }

    /**
     * 指定下标计数自增（0..3）
     */
    @SaCheckLogin
    @PatchMapping("/posts/{id}/reactions")
    public Result<List<Integer>> incrReaction(@PathVariable("id") String extId,
                                              @RequestParam("index") Integer index) {
        if (index == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "index非法");
        }
        return ResultUtils.success(postService.incrReaction(extId, index));
    }

    /**
     * 获取文章评论列表（升序，ext_id）
     */
    @GetMapping("/posts/{id}/comments")
    public Result<List<Comment>> listComments(@PathVariable("id") String extId) {
        return ResultUtils.success(postService.listComments(extId));
    }

    /**
     * 新增评论（需鉴权，ext_id）
     */
    @SaCheckLogin
    @PostMapping("/posts/{id}/comments")
    public Result<Comment> addComment(@PathVariable("id") String extId,
                                      @RequestBody CreateCommentRequest req) {
        long loginId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(postService.addComment(extId, req, loginId));
    }
}
