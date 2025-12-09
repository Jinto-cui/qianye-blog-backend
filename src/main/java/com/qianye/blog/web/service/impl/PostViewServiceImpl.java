package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.PostViewMapper;
import com.qianye.blog.web.model.PostView;
import com.qianye.blog.web.service.PostViewService;
import org.springframework.stereotype.Service;

@Service
public class PostViewServiceImpl extends ServiceImpl<PostViewMapper, PostView>
        implements PostViewService {
}