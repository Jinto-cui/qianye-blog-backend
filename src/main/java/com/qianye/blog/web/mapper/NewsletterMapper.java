package com.qianye.blog.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.blog.web.model.entity.Newsletter;

/**
 * 简报推送 Mapper
 *
 * @author: Jinto Cui
 * @desc: 每次简报推送的历史记录，sent_at 区分草稿与已发送
 * @date: 2026/05/20
 * @version: v2.0
 */
public interface NewsletterMapper extends BaseMapper<Newsletter> {
}