package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.User;
import com.qianye.blog.web.model.dto.UserDto;
import com.qianye.blog.web.model.request.UserLoginRequest;
import com.qianye.blog.web.model.request.UserRegisterRequest;
import com.qianye.blog.web.service.UserService;
import com.qianye.blog.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("rest/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest req) {
        if (req == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAllBlank(req.getUserAccount(), req.getUserPassword(), req.getCheckPassword())) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(
                req.getUserAccount(), req.getUserPassword(), req.getCheckPassword(),
                req.getNickname(), req.getEmail());
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public Result<UserDto> userLogin(@RequestBody UserLoginRequest req, HttpServletRequest request) {
        if (req == null || StringUtils.isAllBlank(req.getUserAccount(), req.getUserPassword())) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        User user = userService.doLogin(req.getUserAccount(), req.getUserPassword(), request);
        UserDto dto = toDto(user);
        dto.setToken(StpUtil.getTokenValue());
        return ResultUtils.success(dto);
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public Result<Void> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return ResultUtils.success(null);
    }

    @GetMapping("/current")
    @SaCheckLogin
    public Result<UserDto> currentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        return ResultUtils.success(toDto(user));
    }

    @GetMapping("/search")
    @SaCheckRole("admin")
    public Result<List<UserDto>> searchUsers(String keyword) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            qw.and(w -> w.like("nickname", keyword).or().like("user_account", keyword));
        }
        List<UserDto> list = userService.list(qw).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    @SaCheckRole("admin")
    public Result<Boolean> deleteUser(@RequestBody long id) {
        if (id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.removeById(id));
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
