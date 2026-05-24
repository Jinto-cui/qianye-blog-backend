package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.web.mapper.SiteConfigMapper;
import com.qianye.blog.web.model.entity.SiteConfig;
import com.qianye.blog.web.service.SiteConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 站点配置服务实现
 *
 * @author: Jinto Cui
 * @desc: 单行表实现，id 固定为 1
 * @date: 2026/05/22
 * @version: v1.0
 */
@Service
@Slf4j
public class SiteConfigServiceImpl extends ServiceImpl<SiteConfigMapper, SiteConfig>
        implements SiteConfigService {

    private static final Long CONFIG_ID = 1L;

    @Override
    public SiteConfig getConfig() {
        SiteConfig config = getById(CONFIG_ID);
        if (config == null) {
            log.warn("站点配置行不存在，返回空对象");
            config = new SiteConfig();
            config.setId(CONFIG_ID);
        }
        return config;
    }
}
