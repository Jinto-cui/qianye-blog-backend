package com.qianye.blog.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qianye.blog.common.Result;
import com.qianye.blog.oss.OssClient;
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
 * @desc: 返回首页所需的站点级配置，image key 通过 OssClient 生成签名 URL
 * @date: 2026/05/22
 * @version: v1.1
 */
@RestController
@RequestMapping("/rest/v1/site")
public class SiteConfigController {

    /** 签名 URL 有效期：1 小时 */
    private static final long URL_EXPIRE_SECONDS = 3600;

    @Autowired
    private SiteConfigService siteConfigService;
    @Autowired
    private OssClient ossClient;

    @GetMapping("/config")
    public Result<Map<String, Object>> getSiteConfig() {
        SiteConfig config = siteConfigService.getConfig();
        String baseUrl = config.getOssBaseUrl();

        List<String> heroPhotos = new ArrayList<>();
        JSONArray heroArr = parseJsonArray(config.getHeroPhotos());
        if (heroArr != null) {
            for (int i = 0; i < heroArr.size(); i++) {
                heroPhotos.add(ossClient.getAccessUrl(heroArr.getString(i), URL_EXPIRE_SECONDS));
            }
        }

        JSONArray resumeArr = parseJsonArray(config.getResume());
        List<JSONObject> resume = new ArrayList<>();
        if (resumeArr != null) {
            for (int i = 0; i < resumeArr.size(); i++) {
                JSONObject item = resumeArr.getJSONObject(i);
                if (item.containsKey("logoKey")) {
                    item.put("logoUrl", ossClient.getAccessUrl(item.getString("logoKey"), URL_EXPIRE_SECONDS));
                    item.remove("logoKey");
                }
                resume.add(item);
            }
        }

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
}
