package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章详情响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 在列表字段基础上补充 Markdown 正文、目录和相关文章
 * @date: 2026/06/04 23:40
 * @version: v1.1
 */
@Data
public class PostDetailDto extends PostDto {
    private Object body;
    private List<Object> headings;
    private List<PostDto> related;
}
