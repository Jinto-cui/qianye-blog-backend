package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Category;
import com.qianye.blog.web.model.request.CategoryPageRequest;
import com.qianye.blog.web.service.CategoryService;
import com.qianye.blog.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类接口
 * 提供分类的增删改查与分页查询
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param entity 分类实体
     * @return 主键ID
     */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody Category entity) {
        categoryService.save(entity);
        return ResultUtils.success(entity.getId());
    }

    /**
     * 根据ID获取分类
     * @param id 主键ID
     * @return 分类详情
     */
    @GetMapping("/get")
    public Result<Category> get(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(categoryService.getById(id));
    }

    /**
     * 列出所有分类
     * @return 分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return ResultUtils.success(categoryService.list());
    }

    /**
     * 分页查询分类
     * 支持按标题模糊查询
     * @param req 分页与筛选参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public Result<Page<Category>> page(@RequestBody CategoryPageRequest req) {
        int pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
        Page<Category> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Category> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(req.getTitle())) {
            qw.like("title", req.getTitle());
        }
        return ResultUtils.success(categoryService.page(page, qw));
    }

    /**
     * 更新分类
     * @param entity 分类实体（需包含主键）
     * @return 是否更新成功
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody Category entity) {
        if (entity.getId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(categoryService.updateById(entity));
    }

    /**
     * 删除分类（逻辑删除）
     * @param id 主键ID
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody Long id) {
        if (id == null || id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(categoryService.removeById(id));
    }
}