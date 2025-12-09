package com.qianye.blog.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.SiteSetting;
import com.qianye.blog.web.model.dto.ProjectDto;
import com.qianye.blog.web.model.dto.ResumeItemDto;
import com.qianye.blog.web.model.dto.SettingsDto;
import com.qianye.blog.web.service.SiteSettingService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 站点设置接口
 * 维护单行 JSON 的站点配置
 */
@RestController
@RequestMapping("/rest/v1")
public class SiteSettingController {
    @Autowired
    private SiteSettingService siteSettingService;

    /**
     * 站点设置（项目/首页图片/简历）
     */
    @GetMapping("/settings")
    public Result<SettingsDto> getSettings() {
        SiteSetting s = siteSettingService.getById(1);
        SettingsDto dto = new SettingsDto();
        if (s != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (s.getProjects() != null) {
                    java.util.List<java.util.Map<String, Object>> arr = mapper.readValue(s.getProjects(), java.util.List.class);
                    java.util.List<ProjectDto> list = new java.util.ArrayList<>();
                    for (java.util.Map<String, Object> it : arr) {
                        ProjectDto p = new ProjectDto();
                        p.set_id((String) it.getOrDefault("_id", java.util.UUID.randomUUID().toString()));
                        p.setName((String) it.getOrDefault("name", ""));
                        p.setUrl((String) it.getOrDefault("url", ""));
                        p.setDescription((String) it.getOrDefault("description", ""));
                        Object iconUrl = it.get("iconUrl");
                        p.setIconUrl(iconUrl == null ? null : iconUrl.toString());
                        list.add(p);
                    }
                    dto.setProjects(list);
                }
            } catch (Exception ignored) {}
            try {
                if (s.getHeroPhotos() != null) {
                    java.util.List<String> photos = mapper.readValue(s.getHeroPhotos(), java.util.List.class);
                    dto.setHeroPhotos(photos);
                }
            } catch (Exception ignored) {}
            try {
                if (s.getResume() != null) {
                    java.util.List<java.util.Map<String, Object>> arr = mapper.readValue(s.getResume(), java.util.List.class);
                    java.util.List<ResumeItemDto> list = new java.util.ArrayList<>();
                    for (java.util.Map<String, Object> it : arr) {
                        ResumeItemDto r = new ResumeItemDto();
                        r.setCompany((String) it.getOrDefault("company", ""));
                        r.setTitle((String) it.getOrDefault("title", ""));
                        r.setLogo((String) it.getOrDefault("logo", ""));
                        r.setStart((String) it.getOrDefault("start", ""));
                        Object end = it.get("end");
                        r.setEnd(end == null ? null : end.toString());
                        list.add(r);
                    }
                    dto.setResume(list);
                }
            } catch (Exception ignored) {}
        }
        return ResultUtils.success(dto);
    }
}