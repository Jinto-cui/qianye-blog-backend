package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.NewsletterMapper;
import com.qianye.blog.web.model.Newsletter;
import com.qianye.blog.web.service.NewsletterService;
import org.springframework.stereotype.Service;

@Service
public class NewsletterServiceImpl extends ServiceImpl<NewsletterMapper, Newsletter>
        implements NewsletterService {
}