package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.entity.Guestbook;
import com.qianye.blog.web.model.entity.User;
import com.qianye.blog.web.model.request.CreateGuestbookRequest;
import com.qianye.blog.web.service.GuestbookService;
import com.qianye.blog.web.service.UserService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 留言墙公开接口
 *
 * @author: Jinto Cui
 * @desc: 公开读取留言列表，登录用户可发表留言，留言用户通过 user_id 关联 user 表
 * @date: 2026/06/04 23:50
 * @version: v1.1
 */
@RestController
@RequestMapping("/rest/v1")
public class GuestbookController {

    @Autowired
    private GuestbookService guestbookService;

    @Autowired
    private UserService userService;

    @GetMapping("/guestbook")
    public Result<List<Guestbook>> listGuestbook() {
        QueryWrapper<Guestbook> qw = new QueryWrapper<Guestbook>().orderByDesc("created_at");
        return ResultUtils.success(guestbookService.list(qw));
    }

    @SaCheckLogin
    @PostMapping("/guestbook")
    public Result<Guestbook> addGuestbook(@RequestBody CreateGuestbookRequest req) {
        long loginId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(loginId);
        if (user == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN, "用户不存在");
        }

        Guestbook last = guestbookService.getOne(new QueryWrapper<Guestbook>()
                .eq("user_id", loginId)
                .orderByDesc("created_at")
                .last("limit 1"));
        if (last != null) {
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "操作过于频繁，请稍后再试");
        }

        Guestbook gb = new Guestbook();
        gb.setUserId(StpUtil.getLoginIdAsLong());
        gb.setBody(req.getMessage());
        guestbookService.save(gb);
        return ResultUtils.success(gb);
    }
}
