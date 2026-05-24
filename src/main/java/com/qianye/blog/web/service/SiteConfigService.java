package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.entity.SiteConfig;

/**
 * 站点配置服务
 *
 * @author: Jinto Cui
 * @desc: 单行表，始终操作 id=1
 * @date: 2026/05/22
 * @version: v1.0
 */
public interface SiteConfigService extends IService<SiteConfig> {

    /**
     * 获取站点配置（单行，id=1）
     */
    SiteConfig getConfig();

    /**
     * 更新站点配置（单行，id 固定为 1）
     */
    void updateConfig(SiteConfig config);
}
