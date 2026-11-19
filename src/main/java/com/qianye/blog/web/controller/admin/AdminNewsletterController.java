package com.qianye.blog.web.controller.admin;

import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.dto.NewsletterDto;
import com.qianye.blog.web.service.NewsletterService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 — 简报推送管理
 *
 * @author: Jinto Cui
 * @desc: 历史分页、创建并发送、删除；当前邮件发送为日志占位实现。
 * @date: 2026/11/19 22:50
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin/newsletters")
public class AdminNewsletterController {

    @Autowired
    private NewsletterService newsletterService;

    @GetMapping
    public Result<List<NewsletterDto>> listNewsletters(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return ResultUtils.success(newsletterService.listAdmin(page, size));
    }

    @GetMapping("/count")
    public Result<Long> countNewsletters() {
        return ResultUtils.success(newsletterService.countAdmin());
    }

    @PostMapping
    public Result<NewsletterDto> createNewsletter(@RequestBody Map<String, Object> body) {
        String subject = body == null ? null : (String) body.get("subject");
        String content = body == null ? null : (String) body.get("body");
        return ResultUtils.success(newsletterService.createAndSend(subject, content));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNewsletter(@PathVariable Long id) {
        newsletterService.deleteNewsletter(id);
        return ResultUtils.success(null);
    }
}
