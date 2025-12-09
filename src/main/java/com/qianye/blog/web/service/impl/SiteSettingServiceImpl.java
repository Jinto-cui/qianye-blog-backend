package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.SiteSettingMapper;
import com.qianye.blog.web.model.SiteSetting;
import com.qianye.blog.web.service.SiteSettingService;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingServiceImpl extends ServiceImpl<SiteSettingMapper, SiteSetting>
        implements SiteSettingService {
}