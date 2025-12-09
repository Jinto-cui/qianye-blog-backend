package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.PostMapper;
import com.qianye.blog.web.model.Post;
import com.qianye.blog.web.service.PostService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
        implements PostService {
    public void method () {

    }
}