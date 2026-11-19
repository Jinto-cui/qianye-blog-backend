package com.qianye.blog.web.controller;

import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.dto.NewsletterDto;
import com.qianye.blog.web.service.NewsletterService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 历史简报公开接口
 *
 * @author: Jinto Cui
 * @desc: 只返回已发送简报，供 /newsletters 列表与详情页展示。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1")
public class NewsletterPublicController {

    @Autowired
    private NewsletterService newsletterService;

    @GetMapping("/newsletters")
    public Result<List<NewsletterDto>> listNewsletters(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset) {
        return ResultUtils.success(newsletterService.listSent(limit, offset));
    }

    @GetMapping("/newsletters/{id}")
    public Result<NewsletterDto> getNewsletter(@PathVariable Long id) {
        return ResultUtils.success(newsletterService.getSent(id));
    }
}
