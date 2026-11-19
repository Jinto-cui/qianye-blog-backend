package com.qianye.blog.web.controller;

import com.qianye.blog.common.Result;
import com.qianye.blog.web.model.dto.ProjectDto;
import com.qianye.blog.web.service.ProjectService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 项目展示公开接口
 *
 * @author: Jinto Cui
 * @desc: 返回按排序权重倒序的项目列表，图标为 OSS 签名 URL。
 * @date: 2026/11/19 22:30
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 查询项目列表
     *
     * @return 项目列表
     */
    @GetMapping("/projects")
    public Result<List<ProjectDto>> listProjects() {
        return ResultUtils.success(projectService.listPublicProjects());
    }
}
