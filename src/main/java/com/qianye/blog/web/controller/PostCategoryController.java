package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.PostCategory;
import com.qianye.blog.web.service.PostCategoryService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章-分类关系接口
 * 提供文章与分类的关联维护，复合主键操作
 */
@RestController
@RequestMapping("/post-category")
public class PostCategoryController {
    @Autowired
    private PostCategoryService postCategoryService;

    /**
     * 新增文章-分类关系
     * @param entity 关系实体（postId, categoryId）
     * @return 是否新增成功
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody PostCategory entity) {
        return ResultUtils.success(postCategoryService.save(entity));
    }

    /**
     * 获取文章-分类关系
     * @param postId 文章ID
     * @param categoryId 分类ID
     * @return 关系实体
     */
    @GetMapping("/get")
    public Result<PostCategory> get(@RequestParam Long postId, @RequestParam Long categoryId) {
        if (postId == null || categoryId == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<PostCategory> qw = new QueryWrapper<>();
        qw.eq("post_id", postId).eq("category_id", categoryId);
        return ResultUtils.success(postCategoryService.getOne(qw));
    }

    /**
     * 列出所有文章-分类关系
     * @return 列表
     */
    @GetMapping("/list")
    public Result<List<PostCategory>> list() {
        return ResultUtils.success(postCategoryService.list());
    }

    /**
     * 删除文章-分类关系
     * @param entity 关系实体（postId, categoryId）
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody PostCategory entity) {
        if (entity.getPostId() == null || entity.getCategoryId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<PostCategory> qw = new QueryWrapper<>();
        qw.eq("post_id", entity.getPostId()).eq("category_id", entity.getCategoryId());
        return ResultUtils.success(postCategoryService.remove(qw));
    }
}