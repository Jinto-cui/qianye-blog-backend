package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.dto.SubscriberDto;
import com.qianye.blog.web.model.entity.Subscriber;

import java.util.List;

/**
 * 简报订阅服务
 *
 * @author: Jinto Cui
 * @desc: 订阅采用双确认流程：提交邮箱 → 发送确认链接 → 确认后计入有效订阅；支持退订和后台管理。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
public interface SubscriberService extends IService<Subscriber> {

    /**
     * 提交订阅并发送确认邮件
     */
    void subscribe(String email);

    /**
     * 确认订阅
     *
     * @return 脱敏邮箱，供确认页展示
     */
    String confirm(String token);

    /**
     * 退订
     *
     * @return 脱敏邮箱
     */
    String unsubscribe(String token);

    /**
     * 已确认且未退订的订阅者数量
     */
    long countActive();

    /**
     * 后台订阅者分页
     */
    List<SubscriberDto> listSubscribers(int page, int size, String keyword);

    /**
     * 后台订阅者数量
     */
    long countSubscribers(String keyword);

    /**
     * 后台删除订阅者
     */
    void deleteSubscriber(Long id);
}
