package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Guestbook;
import com.qianye.blog.web.model.User;
import com.qianye.blog.web.model.request.CreateGuestbookRequest;
import com.qianye.blog.web.service.GuestbookService;
import com.qianye.blog.web.service.UserService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        String userAccount = user != null ? user.getUserAccount() : String.valueOf(loginId);

        Guestbook last = guestbookService.getOne(new QueryWrapper<Guestbook>()
                .eq("user_id", userAccount)
                .orderByDesc("created_at")
                .last("limit 1"));
        if (last != null) {
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "操作过于频繁，请稍后再试");
        }

        Guestbook gb = new Guestbook();
        gb.setUserId(userAccount);
        gb.setUserInfo(req.getUserInfo());
        gb.setMessage(req.getMessage());
        guestbookService.save(gb);
        return ResultUtils.success(gb);
    }
}
