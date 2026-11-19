package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.dto.ProjectDto;
import com.qianye.blog.web.model.entity.Project;

import java.util.List;
import java.util.Map;

/**
 * 项目展示服务
 *
 * @author: Jinto Cui
 * @desc: 公开列表按排序权重倒序返回，后台维护项目基础信息和图标 key。
 * @date: 2026/11/19 22:30
 * @version: v1.0
 */
public interface ProjectService extends IService<Project> {

    /**
     * 公开项目列表
     *
     * @return 按 sortOrder 倒序的项目 DTO
     */
    List<ProjectDto> listPublicProjects();

    /**
     * 后台项目列表，包含 raw iconKey
     */
    List<Map<String, Object>> listAdminProjects();

    /**
     * 后台创建项目
     */
    Map<String, Object> createProject(Map<String, Object> body);

    /**
     * 后台更新项目
     */
    Map<String, Object> updateProject(Long id, Map<String, Object> body);

    /**
     * 后台逻辑删除项目
     */
    void deleteProject(Long id);
}
