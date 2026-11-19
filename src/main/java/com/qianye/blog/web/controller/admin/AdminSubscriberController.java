package com.qianye.blog.web.controller.admin;

import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.dto.SubscriberDto;
import com.qianye.blog.web.service.SubscriberService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理后台 — 订阅者管理
 *
 * @author: Jinto Cui
 * @desc: 邮箱分页查询、计数和逻辑删除，受 /rest/v1/admin/** admin 拦截器保护。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin/subscribers")
public class AdminSubscriberController {

    @Autowired
    private SubscriberService subscriberService;

    @GetMapping
    public Result<List<SubscriberDto>> listSubscribers(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return ResultUtils.success(subscriberService.listSubscribers(page, size, keyword));
    }

    @GetMapping("/count")
    public Result<Long> countSubscribers(@RequestParam(required = false) String keyword) {
        return ResultUtils.success(subscriberService.countSubscribers(keyword));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSubscriber(@PathVariable Long id) {
        subscriberService.deleteSubscriber(id);
        return ResultUtils.success(null);
    }
}
