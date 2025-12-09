package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.PostCategoryMapper;
import com.qianye.blog.web.model.PostCategory;
import com.qianye.blog.web.service.PostCategoryService;
import org.springframework.stereotype.Service;

@Service
public class PostCategoryServiceImpl extends ServiceImpl<PostCategoryMapper, PostCategory>
        implements PostCategoryService {
}