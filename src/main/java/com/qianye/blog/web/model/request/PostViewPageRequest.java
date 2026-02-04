package com.qianye.blog.web.model.request;

import lombok.Data;

@Data
public class PostViewPageRequest {
    private Integer pageNum;
    private Integer pageSize;
    private Long postId;
}