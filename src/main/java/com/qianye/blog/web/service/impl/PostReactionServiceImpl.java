package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.PostReactionMapper;
import com.qianye.blog.web.model.PostReaction;
import com.qianye.blog.web.service.PostReactionService;
import org.springframework.stereotype.Service;

@Service
public class PostReactionServiceImpl extends ServiceImpl<PostReactionMapper, PostReaction>
        implements PostReactionService {
}