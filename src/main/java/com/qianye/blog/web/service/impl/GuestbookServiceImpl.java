package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.GuestbookMapper;
import com.qianye.blog.web.model.Guestbook;
import com.qianye.blog.web.service.GuestbookService;
import org.springframework.stereotype.Service;

@Service
public class GuestbookServiceImpl extends ServiceImpl<GuestbookMapper, Guestbook>
        implements GuestbookService {
}