package com.qianye.blog.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qianye.blog.web.model.Guestbook;

/**
 * 留言墙 Mapper
 *
 * @author: Jinto Cui
 * @desc: user_id 关联 user 表，查询时 JOIN 获取头像昵称
 * @date: 2026/05/20
 * @version: v2.0
 */
public interface GuestbookMapper extends BaseMapper<Guestbook> {
}