package com.qianye.blog.web.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.entity.SiteConfig;
import com.qianye.blog.web.service.SiteConfigService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理后台 — 站点配置 CRUD
 *
 * @author: Jinto Cui
 * @desc: 读取原始配置（OSS key，不拼 URL），更新单行 site_config
 * @date: 2026/05/25 14:00
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminSiteConfigController {

    @Autowired
    private SiteConfigService siteConfigService;

    /**
     * 获取原始站点配置。
     * 与公开接口的区别：返回 raw OSS key（非完整 URL），供管理后台编辑。
     */
    @GetMapping("/site-config")
    public Result<Map<String, Object>> getConfig() {
        SiteConfig config = siteConfigService.getConfig();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("heroPhotos", parseJsonArray(config.getHeroPhotos()));
        data.put("resume", parseJsonArray(config.getResume()));
        data.put("socialLinks", parseJsonArray(config.getSocialLinks()));
        data.put("ossBaseUrl", config.getOssBaseUrl());
        return ResultUtils.success(data);
    }

    /**
     * 更新站点配置。仅更新请求中包含的字段，未传字段保持不变。
     */
    @PutMapping("/site-config")
    public Result<Void> updateConfig(@RequestBody Map<String, Object> body) {
        SiteConfig config = siteConfigService.getConfig();

        if (body.containsKey("heroPhotos")) {
            config.setHeroPhotos(JSON.toJSONString(body.get("heroPhotos")));
        }
        if (body.containsKey("resume")) {
            config.setResume(JSON.toJSONString(body.get("resume")));
        }
        if (body.containsKey("socialLinks")) {
            config.setSocialLinks(JSON.toJSONString(body.get("socialLinks")));
        }
        if (body.containsKey("ossBaseUrl")) {
            config.setOssBaseUrl((String) body.get("ossBaseUrl"));
        }

        siteConfigService.updateConfig(config);
        return ResultUtils.success(null);
    }

    private Object parseJsonArray(String json) {
        if (json == null || json.isEmpty() || "null".equals(json)) return new JSONArray();
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}
