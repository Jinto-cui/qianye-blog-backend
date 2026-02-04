package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.util.List;

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