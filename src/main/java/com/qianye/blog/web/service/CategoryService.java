package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.entity.Category;

import java.util.List;
import java.util.Map;

/**
 * 文章分类服务
 *
 * @author: Jinto Cui
 * @desc: 公开与后台共用分类数据，后台负责新增、更新和删除，名称/slug 保持唯一。
 * @date: 2026/11/19 23:10
 * @version: v1.1
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分类列表，按排序权重倒序
     */
    List<Category> listCategories();

    /**
     * 后台新增分类
     */
    Category createCategory(Map<String, Object> body);

    /**
     * 后台更新分类
     */
    Category updateCategory(Long id, Map<String, Object> body);

    /**
     * 后台删除分类，分类下仍有文章时拒绝
     */
    void deleteCategory(Long id);
}
