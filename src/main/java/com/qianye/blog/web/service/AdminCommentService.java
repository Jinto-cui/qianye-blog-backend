package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.blog.web.model.dto.AdminCommentDto;

/**
 * 管理后台评论服务。
 *
 * @author: Jinto Cui
 * @desc: 提供后台评论分页、计数和删除能力，隔离旧 legacy CRUD 接口。
 * @date: 2026/06/30 00:31
 * @version: v1.0
 */
public interface AdminCommentService {

    /**
     * 分页查询后台评论列表。
     *
     * @param page 页码，从 1 开始
     * @param size 每页数量
     * @param postId 可选文章 ID
     * @param keyword 可选关键词，匹配评论正文或文章标题
     * @return 评论分页
     */
    Page<AdminCommentDto> listComments(int page, int size, Long postId, String keyword);

    /**
     * 查询后台评论数量。
     *
     * @param postId 可选文章 ID
     * @param keyword 可选关键词，匹配评论正文或文章标题
     * @return 评论数量
     */
    Long countComments(Long postId, String keyword);

    /**
     * 删除评论。
     *
     * @param id 评论 ID
     */
    void deleteComment(Long id);
}
