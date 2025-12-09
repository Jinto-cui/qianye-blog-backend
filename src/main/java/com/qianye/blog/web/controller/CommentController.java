package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Comment;
import com.qianye.blog.web.model.request.CommentPageRequest;
import com.qianye.blog.web.service.CommentService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论接口
 * 提供评论的增删改查与分页查询
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 新增评论
     * @param entity 评论实体
     * @return 主键ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody Comment entity) {
        commentService.save(entity);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 根据ID获取评论
     * @param id 主键ID
     * @return 评论详情
     */
    @GetMapping("/get")
    public Result<Comment> get(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(commentService.getById(id));
    }

    /**
     * 列出所有评论
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<Comment>> list() {
        return ResultUtils.success(commentService.list());
    }

    /**
     * 分页查询评论
     * 支持按文章ID、父评论ID过滤
     * @param req 分页与筛选参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<Page<Comment>> page(@RequestBody CommentPageRequest req) {
        int pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        if (req.getPostId() != null) {
            qw.eq("post_id", req.getPostId());
        }
        if (req.getParentId() != null) {
            qw.eq("parent_id", req.getParentId());
        }
        return ResultUtils.success(commentService.page(page, qw));
    }

    /**
     * 更新评论
     * @param entity 评论实体（需包含主键）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Comment entity) {
        if (entity.getId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(commentService.updateById(entity));
    }

    /**
     * 删除评论（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(commentService.removeById(id));
    }
}