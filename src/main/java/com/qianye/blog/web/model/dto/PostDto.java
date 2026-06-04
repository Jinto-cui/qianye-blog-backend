package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 文章列表响应 DTO
 *
 * @author: Jinto Cui
 * @desc: 对齐前端文章卡片字段，_id 返回 post.id 字符串，主图由 OSS key 转换为可访问 URL
 * @date: 2026/06/04 23:40
 * @version: v1.1
 */
@Data
public class PostDto {
    private String _id;
    private String title;
    private String slug;
    private MainImage mainImage;
    private String publishedAt;
    private String description;
    private List<String> categories;
    private Integer readingTime;
    private String mood;

    @Data
    public static class MainImage {
        private String _ref;
        private Asset asset;
    }

    @Data
    public static class Asset {
        private String url;
        private String lqip;
        private Dominant dominant;
    }

    @Data
    public static class Dominant {
        private String background;
        private String foreground;
    }
}
