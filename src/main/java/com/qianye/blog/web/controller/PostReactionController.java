package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.entity.PostReaction;
import com.qianye.blog.web.service.PostReactionService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章反应旧管理接口
 *
 * @author: Jinto Cui
 * @desc: 临时保留旧反应记录维护能力，统一收口到 admin legacy 前缀并要求 admin 角色
 * @date: 2026/06/04 23:45
 * @version: v1.1
 */
@RestController
@RequestMapping("/rest/v1/admin/legacy/post-reaction")
@SaCheckRole("admin")
public class PostReactionController {
    @Autowired
    private PostReactionService postReactionService;

    /**
     * 新增文章反应记录
     * @param entity 文章反应实体
     * @return 主键 ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody PostReaction entity) {
        postReactionService.save(entity);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 获取表情计数
     * @param postId 文章ID
     * @return 表情计数详情
     */
    @GetMapping("/get")
    public Result<PostReaction> get(@RequestParam Long postId) {
        if (postId == null || postId <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<PostReaction> qw = new QueryWrapper<PostReaction>()
                .eq("post_id", postId)
                .last("limit 1");
        return ResultUtils.success(postReactionService.getOne(qw));
    }

    /**
     * 列出所有表情计数
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<PostReaction>> list() {
        return ResultUtils.success(postReactionService.list());
    }

    /**
     * 更新文章反应记录
     * @param entity 文章反应实体（需包含主键）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody PostReaction entity) {
        if (entity.getId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postReactionService.updateById(entity));
    }

    /**
     * 删除文章反应记录（逻辑删除）
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postReactionService.removeById(id));
    }
}
