package com.qianye.blog.web.model.request;

import lombok.Data;

@Data
public class CreateGuestbookRequest {
    private String message;
    private String userInfo;
}