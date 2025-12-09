package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.SubscribedUserMapper;
import com.qianye.blog.web.model.SubscribedUser;
import com.qianye.blog.web.service.SubscribedUserService;
import org.springframework.stereotype.Service;

@Service
public class SubscribedUserServiceImpl extends ServiceImpl<SubscribedUserMapper, SubscribedUser>
        implements SubscribedUserService {
}