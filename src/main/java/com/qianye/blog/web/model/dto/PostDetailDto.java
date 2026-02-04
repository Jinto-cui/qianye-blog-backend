package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostDetailDto extends PostDto {
    private Object body;
    private List<Object> headings;
    private List<PostDto> related;
}