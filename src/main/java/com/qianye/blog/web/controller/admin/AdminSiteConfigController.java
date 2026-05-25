package com.qianye.blog.web.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianye.blog.common.Result;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.model.entity.SiteConfig;
import com.qianye.blog.web.service.SiteConfigService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 — 站点配置 CRUD
 *
 * @author: Jinto Cui
 * @desc: 同时返回 raw key（供编辑）和签名 URL（供预览）
 * @date: 2026/05/25 14:00
 * @version: v1.1
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminSiteConfigController {

    private static final long URL_EXPIRE_SECONDS = 3600;

    @Autowired
    private SiteConfigService siteConfigService;
    @Autowired
    private OssClient ossClient;

    /**
     * 获取站点配置。返回 raw key 和签名预览 URL 两套数据。
     */
    @GetMapping("/site-config")
    public Result<Map<String, Object>> getConfig() {
        SiteConfig config = siteConfigService.getConfig();

        // heroPhotos: raw keys + signed preview URLs
        JSONArray heroKeys = parseJsonArray(config.getHeroPhotos());
        List<String> heroPhotoUrls = new ArrayList<>();
        for (int i = 0; i < heroKeys.size(); i++) {
            heroPhotoUrls.add(ossClient.getAccessUrl(heroKeys.getString(i), URL_EXPIRE_SECONDS));
        }

        // resume: raw data + signed logo URLs
        JSONArray resumeArr = parseJsonArray(config.getResume());
        List<JSONObject> resume = new ArrayList<>();
        for (int i = 0; i < resumeArr.size(); i++) {
            JSONObject item = resumeArr.getJSONObject(i);
            if (item.containsKey("logoKey")) {
                item.put("logoUrl", ossClient.getAccessUrl(item.getString("logoKey"), URL_EXPIRE_SECONDS));
            }
            resume.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("heroPhotos", heroKeys);
        data.put("heroPhotoUrls", heroPhotoUrls);
        data.put("resume", resume);
        data.put("socialLinks", parseJsonArray(config.getSocialLinks()));
        data.put("ossBaseUrl", config.getOssBaseUrl());
        return ResultUtils.success(data);
    }

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

    private JSONArray parseJsonArray(String json) {
        if (json == null || json.isEmpty() || "null".equals(json)) return new JSONArray();
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            return new JSONArray();
        }
    }
}
