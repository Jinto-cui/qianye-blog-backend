package com.qianye.blog.web.controller;

import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.PostReaction;
import com.qianye.blog.web.service.PostReactionService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章表情计数接口
 * 提供四种表情计数的维护（👏❤️🔥👍）
 */
@RestController
@RequestMapping("/post-reaction")
public class PostReactionController {
    @Autowired
    private PostReactionService postReactionService;

    /**
     * 新增或初始化表情计数
     * @param entity 表情计数实体（主键为 postId）
     * @return 文章ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody PostReaction entity) {
        postReactionService.save(entity);
        return ResultUtils.success(entity.getPostId());
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
        return ResultUtils.success(postReactionService.getById(postId));
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
     * 更新表情计数
     * @param entity 表情计数实体（需包含 postId）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody PostReaction entity) {
        if (entity.getPostId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postReactionService.updateById(entity));
    }

    /**
     * 删除表情计数（逻辑删除）
     * @param postId 文章ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long postId) {
        if (postId == null || postId <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postReactionService.removeById(postId));
    }
}