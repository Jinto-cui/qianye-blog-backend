package com.qianye.blog.web.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class NewsletterPageRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String subject;
    private Date startSentAt;
    private Date endSentAt;
}