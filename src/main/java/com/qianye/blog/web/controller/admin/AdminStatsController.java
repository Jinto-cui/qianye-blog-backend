package com.qianye.blog.web.controller.admin;

import com.qianye.blog.common.Result;
import com.qianye.blog.utils.ResultUtils;
import com.qianye.blog.web.mapper.SubscriberMapper;
import com.qianye.blog.web.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理后台 — 仪表盘统计
 *
 * @author: Jinto Cui
 * @desc: 返回各业务表的记录数，供 admin dashboard 展示
 * @date: 2026/05/25 14:00
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminStatsController {

    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private GuestbookService guestbookService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SubscriberMapper subscriberMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("postCount", postService.count());
        data.put("commentCount", commentService.count());
        data.put("guestbookCount", guestbookService.count());
        data.put("categoryCount", categoryService.count());
        data.put("subscriberCount", subscriberMapper.selectCount(null));
        return ResultUtils.success(data);
    }
}
