package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.mapper.CategoryMapper;
import com.qianye.blog.web.mapper.PostCategoryMapper;
import com.qianye.blog.web.model.entity.Category;
import com.qianye.blog.web.model.entity.PostCategory;
import com.qianye.blog.web.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 文章分类服务实现
 *
 * @author: Jinto Cui
 * @desc: 分类 CRUD 与唯一性校验；删除前检查是否仍有文章占用，避免文章分类静默丢失。
 * @date: 2026/11/19 23:10
 * @version: v1.1
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    /** 分类名称最大长度，对齐 category.name。 */
    private static final int NAME_MAX_LENGTH = 64;

    /** 分类 slug 最大长度，对齐 category.slug。 */
    private static final int SLUG_MAX_LENGTH = 128;

    @Autowired
    private PostCategoryMapper postCategoryMapper;

    @Override
    public List<Category> listCategories() {
        return list(new QueryWrapper<Category>()
                .orderByDesc("sort_order")
                .orderByAsc("id"));
    }

    @Override
    public Category createCategory(Map<String, Object> body) {
        Category category = new Category();
        fillCategory(category, body, true);
        category.setCreatedAt(new Date());
        category.setUpdatedAt(category.getCreatedAt());
        save(category);
        log.info("后台分类新增, categoryId={}, name={}", category.getId(), category.getName());
        return category;
    }

    @Override
    public Category updateCategory(Long id, Map<String, Object> body) {
        Category category = findCategory(id);
        fillCategory(category, body, false);
        category.setUpdatedAt(new Date());
        updateById(category);
        log.info("后台分类更新, categoryId={}, name={}", category.getId(), category.getName());
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = findCategory(id);
        Long used = postCategoryMapper.selectCount(
                new QueryWrapper<PostCategory>().eq("category_id", id));
        if (used != null && used > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "该分类下还有 " + used + " 篇文章，无法删除");
        }
        removeById(id);
        log.info("后台分类删除, categoryId={}, name={}", id, category.getName());
    }

    private Category findCategory(Long id) {
        Category category = id == null ? null : getById(id);
        if (category == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "分类不存在");
        }
        return category;
    }

    private void fillCategory(Category category, Map<String, Object> body, boolean creating) {
        if (body == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
        }
        if (body.containsKey("name") || creating) {
            String name = StringUtils.trimToEmpty((String) body.get("name"));
            if (name.isEmpty()) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "分类名称不能为空");
            }
            if (name.length() > NAME_MAX_LENGTH) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "分类名称最多 64 个字符");
            }
            checkNameUnique(name, category.getId());
            category.setName(name);
        }
        if (body.containsKey("slug") || creating) {
            String slug = StringUtils.trimToEmpty((String) body.get("slug"));
            if (slug.isEmpty()) {
                slug = generateSlug();
            }
            if (slug.length() > SLUG_MAX_LENGTH) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "分类 slug 最多 128 个字符");
            }
            if (!slug.matches("^[a-zA-Z0-9_-]+$")) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "分类 slug 只能包含字母、数字、下划线和短横线");
            }
            checkSlugUnique(slug, category.getId());
            category.setSlug(slug.toLowerCase());
        }
        if (body.containsKey("description")) {
            category.setDescription(StringUtils.trimToNull((String) body.get("description")));
        }
        if (body.containsKey("sortOrder")) {
            category.setSortOrder(readSortOrder(body.get("sortOrder")));
        } else if (creating) {
            category.setSortOrder(0);
        }
    }

    private void checkNameUnique(String name, Long excludeId) {
        QueryWrapper<Category> qw = new QueryWrapper<Category>().eq("name", name);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        if (count(qw) > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "分类名称已存在");
        }
    }

    private void checkSlugUnique(String slug, Long excludeId) {
        QueryWrapper<Category> qw = new QueryWrapper<Category>().eq("slug", slug.toLowerCase());
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        if (count(qw) > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "分类 slug 已存在");
        }
    }

    private String generateSlug() {
        return "cat-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private Integer readSortOrder(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "排序权重必须是整数");
        }
    }
}
