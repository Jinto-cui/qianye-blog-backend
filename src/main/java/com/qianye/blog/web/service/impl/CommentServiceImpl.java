package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.CommentMapper;
import com.qianye.blog.web.model.Comment;
import com.qianye.blog.web.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {
}