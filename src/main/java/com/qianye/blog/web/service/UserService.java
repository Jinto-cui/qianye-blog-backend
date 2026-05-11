package com.qianye.blog.web.service;

import com.qianye.blog.web.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword,
                      String nickname, String email);

    User doLogin(String userAccount, String userPassword, HttpServletRequest request);

    Integer userLogout(HttpServletRequest request);
}
