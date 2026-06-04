package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.entity.Comment;
import com.qianye.blog.web.model.entity.Post;
import com.qianye.blog.web.model.dto.PostDetailDto;
import com.qianye.blog.web.model.dto.PostDto;
import com.qianye.blog.web.model.request.CreateCommentRequest;

import java.util.List;

/**
 * @author: Jinto Cui
 * @desc: 文章服务接口 — 提供文章的查询、浏览量、表情反应与评论等核心业务能力
 * @date: 2026/06/04 23:58
 * @version: v1.1
 */
public interface PostService extends IService<Post> {

    /**
     * 分页获取文章列表（limit/offset 语义）
     */
    List<PostDto> listPosts(int limit, int offset);

    /**
     * 获取所有文章的 slug 标识
     */
    List<String> listSlugs();

    /**
     * 按 slug 获取文章详情（含正文解析、相关文章推荐）
     */
    PostDetailDto getPostBySlug(String slug);

    /**
     * 递增文章浏览量（按 post.id），同步更新 post.views，返回最新值
     */
    Long incrViews(Long postId);

    /**
     * 批量查询浏览量（post.id 列表），顺序与请求一致
     */
    List<Long> batchViews(List<Long> postIds);

    /**
     * 获取文章表情反应计数，顺序：[clap, heart, fire, thumbsUp]
     */
    List<Integer> getReactions(Long postId);

    /**
     * 指定下标表情计数自增（0=clap, 1=heart, 2=fire, 3=thumbsUp）
     */
    List<Integer> incrReaction(Long postId, int index, long loginId);

    /**
     * 获取文章评论列表（按创建时间升序）
     */
    List<Comment> listComments(Long postId);

    /**
     * 新增评论（支持嵌套回复），返回新建评论实体
     */
    Comment addComment(Long postId, CreateCommentRequest req, long loginId);
}
