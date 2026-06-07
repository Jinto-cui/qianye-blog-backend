package com.qianye.blog.web.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.qianye.blog.common.Result;
import com.qianye.blog.utils.ResultUtils;
import com.qianye.blog.web.service.AdminPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private AdminPostService adminPostService;

    /**
     * 文章列表（含草稿），按更新时间降序
     */
    @GetMapping("/posts")
    public Result<List<Map<String, Object>>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResultUtils.success(adminPostService.listPosts(page, size));
    }

    /** 文章总数 */
    @GetMapping("/posts/count")
    public Result<Long> countPosts() {
        return ResultUtils.success(adminPostService.countPosts());
    }

    /** 单篇文章详情（编辑用） */
    @GetMapping("/posts/{id}")
    public Result<Map<String, Object>> getPost(@PathVariable Long id) {
        return ResultUtils.success(adminPostService.getPost(id));
    }

    /** 新建文章 */
    @PostMapping("/posts")
    public Result<Map<String, Object>> createPost(@RequestBody Map<String, Object> body) {
        return ResultUtils.success(adminPostService.createPost(body, StpUtil.getLoginIdAsLong()));
    }

    /** 更新文章 */
    @PutMapping("/posts/{id}")
    public Result<Map<String, Object>> updatePost(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body) {
        return ResultUtils.success(adminPostService.updatePost(id, body, StpUtil.getLoginIdAsLong()));
    }

    /** 删除文章（逻辑删除） */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        adminPostService.deletePost(id);
        return ResultUtils.success(null);
    }
}
