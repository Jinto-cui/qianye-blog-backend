package com.qianye.blog.web.controller;

import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.PostView;
import com.qianye.blog.web.service.PostViewService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章浏览量接口
 * 提供浏览量的维护（主键为 postId）
 */
@RestController
@RequestMapping("/post-view")
public class PostViewController {
    @Autowired
    private PostViewService postViewService;

    /**
     * 新增或初始化浏览量
     * @param entity 浏览量实体（主键为 postId）
     * @return 文章ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody PostView entity) {
        postViewService.save(entity);
        return ResultUtils.success(entity.getPostId());
    }

    /**
     * 获取浏览量
     * @param postId 文章ID
     * @return 浏览量详情
     */
    @GetMapping("/get")
    public Result<PostView> get(@RequestParam Long postId) {
        if (postId == null || postId <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postViewService.getById(postId));
    }

    /**
     * 列出所有浏览量记录
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<PostView>> list() {
        return ResultUtils.success(postViewService.list());
    }

    /**
     * 更新浏览量
     * @param entity 浏览量实体（需包含 postId）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody PostView entity) {
        if (entity.getPostId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postViewService.updateById(entity));
    }

    /**
     * 删除浏览量记录（逻辑删除）
     * @param postId 文章ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long postId) {
        if (postId == null || postId <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postViewService.removeById(postId));
    }
}