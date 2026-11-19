package com.qianye.blog.web.controller.admin;

import com.qianye.blog.common.Result;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.model.entity.Category;
import com.qianye.blog.web.service.CategoryService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 — 分类列表 + OSS 预览
 *
 * @author: Jinto Cui
 * @desc: 供文章编辑页选择分类 & 根据 key 获取签名预览 URL
 * @date: 2026/05/25 22:00
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private OssClient ossClient;

    /** 全部分类列表 */
    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        return ResultUtils.success(categoryService.listCategories());
    }

    /** 新增分类 */
    @PostMapping("/categories")
    public Result<Category> createCategory(@RequestBody Map<String, Object> body) {
        return ResultUtils.success(categoryService.createCategory(body));
    }

    /** 更新分类 */
    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id,
                                           @RequestBody Map<String, Object> body) {
        return ResultUtils.success(categoryService.updateCategory(id, body));
    }

    /** 删除分类 */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResultUtils.success(null);
    }

    /** 根据 OSS key 返回签名预览 URL */
    @GetMapping("/oss-preview")
    public Result<Map<String, Object>> ossPreview(@RequestParam String key) {
        String url = ossClient.getAccessUrl(key, 3600);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("key", key);
        data.put("url", url);
        return ResultUtils.success(data);
    }
}
