package com.qianye.blog.web.model.request;

import lombok.Data;

@Data
public class SubscribedUserPageRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String userName;
    private String nickName;
    private String email;
    private String status;
}