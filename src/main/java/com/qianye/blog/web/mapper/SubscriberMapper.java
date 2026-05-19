package com.qianye.blog.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.blog.web.model.Subscriber;

/**
 * 简报订阅者 Mapper
 *
 * @author: Jinto Cui
 * @desc: 管理 Newsletter 邮件订阅数据，与旧 SubscribedUser（用户表）解耦
 * @date: 2026/05/20
 * @version: v1.0
 */
public interface SubscriberMapper extends BaseMapper<Subscriber> {
}
