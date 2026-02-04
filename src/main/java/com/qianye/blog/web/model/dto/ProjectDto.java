package com.qianye.blog.web.model.dto;

import lombok.Data;

@Data
public class ProjectDto {
    private String _id;
    private String name;
    private String url;
    private String description;
    private String iconUrl;
}