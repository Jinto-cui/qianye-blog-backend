package com.qianye.blog.web.controller;

import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.request.CreateSubscriptionRequest;
import com.qianye.blog.web.service.SubscriberService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简报订阅公开接口
 *
 * @author: Jinto Cui
 * @desc: 提交订阅、确认、退订和有效订阅人数；确认 token 只通过邮件链接传递。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1")
public class SubscriptionController {

    @Autowired
    private SubscriberService subscriberService;

    /**
     * 提交订阅，后端发送确认邮件
     */
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestBody CreateSubscriptionRequest req) {
        subscriberService.subscribe(req == null ? null : req.getEmail());
        return ResultUtils.success(null);
    }

    /**
     * 确认订阅
     *
     * @return 脱敏邮箱
     */
    @GetMapping("/subscribe/confirm")
    public Result<String> confirm(@RequestParam String token) {
        return ResultUtils.success(subscriberService.confirm(token));
    }

    /**
     * 退订
     *
     * @return 脱敏邮箱
     */
    @GetMapping("/subscribe/unsubscribe")
    public Result<String> unsubscribe(@RequestParam String token) {
        return ResultUtils.success(subscriberService.unsubscribe(token));
    }

    /**
     * 有效订阅人数，用于首页订阅区展示
     */
    @GetMapping("/subscribe/count")
    public Result<Long> count() {
        return ResultUtils.success(subscriberService.countActive());
    }
}
