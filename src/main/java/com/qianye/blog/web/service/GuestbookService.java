package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.dto.GuestbookDto;
import com.qianye.blog.web.model.entity.Guestbook;

import java.util.List;

/**
 * 留言墙服务
 *
 * @author: Jinto Cui
 * @desc: 公开读取按时间倒序分页，登录用户写入前统一做内容安全与频率校验。
 * @date: 2026/11/19 22:10
 * @version: v1.0
 */
public interface GuestbookService extends IService<Guestbook> {

    /**
     * 分页查询留言列表，按创建时间倒序
     *
     * @param limit  单页数量
     * @param offset 偏移量
     * @return 留言 DTO 列表
     */
    List<GuestbookDto> listMessages(int limit, int offset);

    /**
     * 新增留言
     *
     * @param loginId 当前登录用户 ID
     * @param body    留言正文
     * @return 新增后的留言 DTO
     */
    GuestbookDto addMessage(long loginId, String body);
}
