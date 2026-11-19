package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.dto.GuestbookDto;
import com.qianye.blog.web.model.request.CreateGuestbookRequest;
import com.qianye.blog.web.service.GuestbookService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 留言墙公开接口
 *
 * @author: Jinto Cui
 * @desc: 公开读取分页留言，登录用户可发表留言，用户展示信息由后端按 user_id 关联生成。
 * @date: 2026/11/19 22:10
 * @version: v1.2
 */
@RestController
@RequestMapping("/rest/v1")
public class GuestbookController {

    @Autowired
    private GuestbookService guestbookService;

    /**
     * 分页查询留言列表
     *
     * @param limit  单页数量，默认 20，最大 50
     * @param offset 偏移量，默认 0
     * @return 留言列表
     */
    @GetMapping("/guestbook")
    public Result<List<GuestbookDto>> listGuestbook(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset) {
        return ResultUtils.success(guestbookService.listMessages(limit, offset));
    }

    /**
     * 新增留言
     *
     * @param req 留言正文
     * @return 新增后的留言
     */
    @SaCheckLogin
    @PostMapping("/guestbook")
    public Result<GuestbookDto> addGuestbook(@RequestBody CreateGuestbookRequest req) {
        long loginId = StpUtil.getLoginIdAsLong();
        return ResultUtils.success(guestbookService.addMessage(loginId, req == null ? null : req.getBody()));
    }
}
