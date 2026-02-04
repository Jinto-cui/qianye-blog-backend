package com.qianye.blog.web.model.request;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String userInfo;
    private String body;
    private Long parentId;
}