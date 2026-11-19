package com.qianye.blog.web.controller.admin;

import com.qianye.blog.common.Result;
import com.qianye.blog.web.service.ProjectService;
import com.qianye.blog.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 — 项目管理
 *
 * @author: Jinto Cui
 * @desc: 项目列表、新建、更新和逻辑删除，受 /rest/v1/admin/** admin 拦截器保护。
 * @date: 2026/11/19 22:30
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/projects")
    public Result<List<Map<String, Object>>> listProjects() {
        return ResultUtils.success(projectService.listAdminProjects());
    }

    @PostMapping("/projects")
    public Result<Map<String, Object>> createProject(@RequestBody Map<String, Object> body) {
        return ResultUtils.success(projectService.createProject(body));
    }

    @PutMapping("/projects/{id}")
    public Result<Map<String, Object>> updateProject(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> body) {
        return ResultUtils.success(projectService.updateProject(id, body));
    }

    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResultUtils.success(null);
    }
}
