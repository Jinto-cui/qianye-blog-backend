package com.qianye.blog.web.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.web.model.entity.User;
import com.qianye.blog.web.service.UserService;
import com.qianye.blog.web.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    UserMapper userMapper;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,
                             String nickname, String email) {
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户名长度不能小于4位");
        }

        String regEx = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (matcher.find()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户名不能包含特殊字符");
        }

        if (!userPassword.equals(checkPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("user_account", userAccount);
        if (userMapper.selectCount(qw) > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }

        if (StringUtils.isNotBlank(email)) {
            QueryWrapper<User> emailQw = new QueryWrapper<>();
            emailQw.eq("email", email);
            if (userMapper.selectCount(emailQw) > 0) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "邮箱已被注册");
            }
        }

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(passwordEncoder.encode(userPassword));
        user.setNickname(StringUtils.isNotBlank(nickname) ? nickname : userAccount);
        user.setEmail(email);
        this.save(user);
        return user.getId();
    }

    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户名长度不能小于4位");
        }

        String regEx = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (matcher.find()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户名不能包含特殊字符");
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("user_account", userAccount);
        User user = userMapper.selectOne(qw);
        if (user == null) {
            log.info("login failed, account not found: {}", userAccount);
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
            log.info("login failed, password mismatch: {}", userAccount);
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new GlobalException(ErrorCode.NO_AUTH, "账号已被停用");
        }

        StpUtil.login(user.getId());

        // 更新最后登录信息
        user.setLastLoginAt(new Date());
        user.setLastLoginIp(request.getRemoteAddr());
        userMapper.updateById(user);

        return user;
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        StpUtil.logout();
        return 1;
    }
}
