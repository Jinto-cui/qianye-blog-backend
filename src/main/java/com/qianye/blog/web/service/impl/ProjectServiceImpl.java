package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.mapper.ProjectMapper;
import com.qianye.blog.web.model.dto.ProjectDto;
import com.qianye.blog.web.model.entity.Project;
import com.qianye.blog.web.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目展示服务实现
 *
 * @author: Jinto Cui
 * @desc: 项目公开列表与后台 CRUD，图标 key 通过 OssClient 生成签名预览地址。
 * @date: 2026/11/19 22:30
 * @version: v1.0
 */
@Service
@Slf4j
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
        implements ProjectService {

    /** 图标签名 URL 有效期：1 小时。 */
    private static final long URL_EXPIRE_SECONDS = 3600;

    /** 项目名称最大长度。 */
    private static final int NAME_MAX_LENGTH = 128;

    @Autowired
    private OssClient ossClient;

    @Override
    public List<ProjectDto> listPublicProjects() {
        List<Project> projects = list(new QueryWrapper<Project>()
                .orderByDesc("sort_order")
                .orderByDesc("id"));
        return projects.stream().map(this::toPublicDto).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listAdminProjects() {
        List<Project> projects = list(new QueryWrapper<Project>()
                .orderByDesc("sort_order")
                .orderByDesc("id"));
        return projects.stream().map(this::toAdminVo).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> createProject(Map<String, Object> body) {
        Project project = new Project();
        fillProject(project, body);
        project.setSortOrder(readSortOrder(body));
        project.setCreatedAt(new Date());
        project.setUpdatedAt(project.getCreatedAt());
        save(project);
        log.info("后台项目新增, projectId={}, name={}", project.getId(), project.getName());
        return toAdminVo(project);
    }

    @Override
    public Map<String, Object> updateProject(Long id, Map<String, Object> body) {
        Project project = findProject(id);
        fillProject(project, body);
        if (body.containsKey("sortOrder")) {
            project.setSortOrder(readSortOrder(body));
        }
        project.setUpdatedAt(new Date());
        updateById(project);
        log.info("后台项目更新, projectId={}, name={}", project.getId(), project.getName());
        return toAdminVo(project);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = findProject(id);
        removeById(project.getId());
        log.info("后台项目删除, projectId={}, name={}", project.getId(), project.getName());
    }

    private Project findProject(Long id) {
        Project project = id == null ? null : getById(id);
        if (project == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "项目不存在");
        }
        return project;
    }

    /**
     * 校验并写入可编辑字段，空字段允许清空。
     */
    private void fillProject(Project project, Map<String, Object> body) {
        if (body == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "请求体不能为空");
        }
        if (body.containsKey("name")) {
            String name = StringUtils.trimToEmpty((String) body.get("name"));
            if (name.isEmpty()) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "项目名称不能为空");
            }
            if (name.length() > NAME_MAX_LENGTH) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "项目名称最多 128 个字符");
            }
            project.setName(name);
        } else if (project.getId() == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "项目名称不能为空");
        }
        if (body.containsKey("url")) {
            String url = StringUtils.trimToNull((String) body.get("url"));
            if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "项目链接必须以 http:// 或 https:// 开头");
            }
            project.setUrl(url);
        }
        if (body.containsKey("description")) {
            project.setDescription(StringUtils.trimToNull((String) body.get("description")));
        }
        if (body.containsKey("iconKey")) {
            project.setIconKey(StringUtils.trimToNull((String) body.get("iconKey")));
        }
    }

    private Integer readSortOrder(Map<String, Object> body) {
        Object value = body.get("sortOrder");
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "排序权重必须是整数");
        }
    }

    private ProjectDto toPublicDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setUrl(project.getUrl());
        dto.setDescription(project.getDescription());
        dto.setSortOrder(project.getSortOrder());
        dto.setIconUrl(StringUtils.isNotBlank(project.getIconKey())
                ? ossClient.getAccessUrl(project.getIconKey(), URL_EXPIRE_SECONDS)
                : null);
        return dto;
    }

    private Map<String, Object> toAdminVo(Project project) {
        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", project.getId());
        vo.put("name", project.getName());
        vo.put("url", project.getUrl());
        vo.put("description", project.getDescription());
        vo.put("iconKey", project.getIconKey());
        vo.put("iconUrl", StringUtils.isNotBlank(project.getIconKey())
                ? ossClient.getAccessUrl(project.getIconKey(), URL_EXPIRE_SECONDS)
                : null);
        vo.put("sortOrder", project.getSortOrder());
        vo.put("createdAt", project.getCreatedAt());
        vo.put("updatedAt", project.getUpdatedAt());
        return vo;
    }
}
