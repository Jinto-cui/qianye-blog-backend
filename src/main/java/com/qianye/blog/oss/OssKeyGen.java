package com.qianye.blog.oss;

import java.util.UUID;

/**
 * OSS 对象 Key 生成器。
 * 所有 OSS key 规则在此收口，业务代码禁止自行拼接 key 字符串。
 * 格式：{resourceType}/{uuid}.{ext}，保证全局唯一、按资源类型分目录。
 *
 * @author: Jinto Cui
 * @desc: OSS 对象 Key 统一生成工具
 * @date: 2026/05/24 17:00
 * @version: v1.0
 */
public class OssKeyGen {

    /** 文章主图：post/{uuid}.{ext} */
    public static String postImage(String fileName) {
        return String.format("post/%s.%s", UUID.randomUUID().toString(), ext(fileName));
    }

    /** 文章正文图片：post/body/{uuid}.{ext} */
    public static String postBodyImage(String fileName) {
        return String.format("post/body/%s.%s", UUID.randomUUID().toString(), ext(fileName));
    }

    /** 用户头像：avatar/user{userId}/{uuid}.{ext} */
    public static String avatar(Long userId, String fileName) {
        return String.format("avatar/user%d/%s.%s", userId, UUID.randomUUID().toString(), ext(fileName));
    }

    /** 项目图标：project/{uuid}.{ext} */
    public static String projectIcon(String fileName) {
        return String.format("project/%s.%s", UUID.randomUUID().toString(), ext(fileName));
    }

    /** 站点资源：site/{uuid}.{ext} */
    public static String siteAsset(String fileName) {
        return String.format("site/%s.%s", UUID.randomUUID().toString(), ext(fileName));
    }

    /** 临时文件：tmp/{uuid}.{ext}，适用于未归类或待处理的资源 */
    public static String temp(String fileName) {
        return String.format("tmp/%s.%s", UUID.randomUUID().toString(), ext(fileName));
    }

    /** 从文件名提取扩展名，无扩展名默认 jpg */
    private static String ext(String fileName) {
        if (fileName == null) return "jpg";
        int i = fileName.lastIndexOf('.');
        return i > 0 ? fileName.substring(i + 1).toLowerCase() : "jpg";
    }
}
