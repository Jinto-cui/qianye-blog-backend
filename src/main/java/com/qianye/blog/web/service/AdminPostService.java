package com.qianye.blog.web.service;

import java.util.List;
import java.util.Map;

/**
 * 管理后台文章服务。
 *
 * @author: Jinto Cui
 * @desc: 收口后台文章保存事务，保证文章、分类和正文资源状态一致
 * @date: 2026/06/06 11:36
 * @version: v1.0
 */
public interface AdminPostService {

    /**
     * 分页查询后台文章列表。
     */
    List<Map<String, Object>> listPosts(int page, int size);

    /**
     * 查询文章总数。
     */
    Long countPosts();

    /**
     * 查询后台编辑用文章详情。
     */
    Map<String, Object> getPost(Long id);

    /**
     * 创建文章并绑定草稿正文资源。
     */
    Map<String, Object> createPost(Map<String, Object> body, Long loginUserId);

    /**
     * 更新文章并同步正文资源引用。
     */
    Map<String, Object> updatePost(Long id, Map<String, Object> body, Long loginUserId);

    /**
     * 删除文章。
     */
    void deletePost(Long id);
}
