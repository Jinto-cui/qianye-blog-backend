package com.qianye.blog.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.Guestbook;
import com.qianye.blog.web.model.User;
import com.qianye.blog.web.model.request.CreateGuestbookRequest;
import com.qianye.blog.web.service.GuestbookService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 客墙接口
 * 提供留言的增删改查与分页查询
 */
@RestController
@RequestMapping("/rest/v1")
public class GuestbookController {
    @Autowired
    private GuestbookService guestbookService;

    /**
     * 获取客墙留言列表
     */
    @GetMapping("/guestbook")
    public Result<List<Guestbook>> listGuestbook() {
        QueryWrapper<Guestbook> qw = new QueryWrapper<Guestbook>().orderByDesc("created_at");
        return ResultUtils.success(guestbookService.list(qw));
    }

    /**
     * 新增客墙留言（需鉴权，简单限流）
     */
    @PostMapping("/guestbook")
    public Result<Guestbook> addGuestbook(@RequestBody CreateGuestbookRequest req,
                                          HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute("userLoginStatus");
        if (userObj == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN, "未认证");
        }
        User user = (User) userObj;
        Guestbook last = guestbookService.getOne(new QueryWrapper<Guestbook>()
                .eq("user_id", user.getUserAccount())
                .orderByDesc("created_at")
                .last("limit 1"));
        if (last != null) {
            throw new GlobalException(ErrorCode.SYSTEM_ERROR, "触发限流");
        }
        Guestbook gb = new Guestbook();
        gb.setUserId(user.getUserAccount());
        gb.setUserInfo(req.getUserInfo());
        gb.setMessage(req.getMessage());
        guestbookService.save(gb);
        return ResultUtils.success(gb);
    }
}