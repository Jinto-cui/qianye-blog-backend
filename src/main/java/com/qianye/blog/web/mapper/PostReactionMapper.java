package com.qianye.blog.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.blog.web.model.entity.PostReaction;

/**
 * 文章反应 Mapper
 *
 * @author: Jinto Cui
 * @desc: 用户级反应记录，防重复。计数查询通过 COUNT GROUP BY reaction_type
 * @date: 2026/05/20
 * @version: v2.0
 */
public interface PostReactionMapper extends BaseMapper<PostReaction> {
}