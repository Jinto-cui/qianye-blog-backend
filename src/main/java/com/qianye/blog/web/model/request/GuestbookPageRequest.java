package com.qianye.blog.web.model.request;

import lombok.Data;

@Data
public class GuestbookPageRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String userId;
}