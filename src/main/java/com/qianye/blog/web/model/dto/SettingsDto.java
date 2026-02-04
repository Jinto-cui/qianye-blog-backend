package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SettingsDto {
    private List<ProjectDto> projects;
    private List<String> heroPhotos;
    private List<ResumeItemDto> resume;
}