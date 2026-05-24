package com.qianye.blog.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.entity.SiteConfig;
import com.qianye.blog.web.service.SiteConfigService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点配置接口
 *
 * @author: Jinto Cui
 * @desc: 返回首页所需的站点级配置（hero图片、简历、社交链接），image key 自动拼 oss_base_url
 * @date: 2026/05/22
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/site")
public class SiteConfigController {

    @Autowired
    private SiteConfigService siteConfigService;

    /**
     * 获取站点配置
     * 将 OSS key 拼接为完整 URL 后返回，前端直接使用
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getSiteConfig() {
        SiteConfig config = siteConfigService.getConfig();
        String baseUrl = config.getOssBaseUrl();

        // hero_photos: OSS key 数组 → 完整 URL 数组
        List<String> heroPhotos = new ArrayList<>();
        JSONArray heroArr = parseJsonArray(config.getHeroPhotos());
        if (heroArr != null) {
            for (int i = 0; i < heroArr.size(); i++) {
                heroPhotos.add(assembleUrl(baseUrl, heroArr.getString(i)));
            }
        }

        // resume: logoKey → logoUrl
        JSONArray resumeArr = parseJsonArray(config.getResume());
        List<JSONObject> resume = new ArrayList<>();
        if (resumeArr != null) {
            for (int i = 0; i < resumeArr.size(); i++) {
                JSONObject item = resumeArr.getJSONObject(i);
                if (item.containsKey("logoKey")) {
                    item.put("logoUrl", assembleUrl(baseUrl, item.getString("logoKey")));
                    item.remove("logoKey");
                }
                resume.add(item);
            }
        }

        // social_links: 直接返回（存的就是 URL，非 OSS key）
        Object socialLinks = parseJsonArray(config.getSocialLinks());

        Map<String, Object> data = new HashMap<>();
        data.put("heroPhotos", heroPhotos);
        data.put("resume", resume);
        data.put("socialLinks", socialLinks != null ? socialLinks : new ArrayList<>());
        data.put("ossBaseUrl", baseUrl != null ? baseUrl : "");

        return ResultUtils.success(data);
    }

    private JSONArray parseJsonArray(String json) {
        if (json == null || json.isEmpty() || "null".equals(json)) {
            return null;
        }
        try {
            return JSON.parseArray(json);
        } catch (Exception e) {
            return null;
        }
    }

    private String assembleUrl(String baseUrl, String key) {
        if (key == null || key.isEmpty()) return "";
        if (baseUrl == null || baseUrl.isEmpty()) return key;
        if (key.startsWith("http://") || key.startsWith("https://")) return key;
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = key.startsWith("/") ? key : "/" + key;
        return base + path;
    }
}
