package com.qianye.blog.web.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class PostPageRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
    private String mood;
    private Date startPublishedAt;
    private Date endPublishedAt;
}