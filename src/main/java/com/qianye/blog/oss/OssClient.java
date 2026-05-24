package com.qianye.blog.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.qianye.blog.config.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 阿里云 OSS 客户端封装。
 * 业务代码不直接依赖 OSS SDK，统一通过此类操作对象存储。
 * 提供上传、删除、存在性检查、对象读取、签名 URL 生成等能力。
 * 签名 URL 自动替换为 CDN 域名（若已配置），并支持拼接图片处理参数。
 *
 * @author: Jinto Cui
 * @desc: 阿里云 OSS 客户端统一封装
 * @date: 2026/05/24 17:00
 * @version: v1.0
 */
@Slf4j
@Component
public class OssClient {

    private final OssConfig config;
    private OSS oss;

    public OssClient(OssConfig config) {
        this.config = config;
    }

    /** 初始化 OSS SDK 客户端，配置不完整时跳过并告警 */
    @PostConstruct
    public void init() {
        String ep = config.getEndpoint();
        if (ep != null && !ep.isEmpty()
                && config.getAccessKeyId() != null && !config.getAccessKeyId().isEmpty()
                && config.getAccessKeySecret() != null && !config.getAccessKeySecret().isEmpty()) {
            this.oss = new OSSClientBuilder().build(ep, config.getAccessKeyId(), config.getAccessKeySecret());
            log.info("OSS client 初始化完成, endpoint={}, bucket={}", ep, config.getBucket());
        } else {
            log.warn("OSS 配置不完整，client 未初始化。缺少 endpoint/accessKeyId/accessKeySecret");
        }
    }

    /** 关闭 OSS SDK 客户端，释放连接资源 */
    @PreDestroy
    public void shutdown() {
        if (oss != null) {
            oss.shutdown();
        }
    }

    /** 获取客户端实例，未初始化时抛异常 */
    private OSS oss() {
        if (oss == null) {
            throw new IllegalStateException("OSS client 未初始化，请检查 oss 相关配置");
        }
        return oss;
    }

    // ==================== 基础操作 ====================

    /**
     * 上传 InputStream 到 OSS。
     *
     * @param in          输入流
     * @param key         对象 key
     * @param contentType MIME 类型，可选
     * @return 对象的 key
     */
    public String upload(InputStream in, String key, String contentType) {
        ObjectMetadata meta = new ObjectMetadata();
        if (contentType != null && !contentType.isEmpty()) {
            meta.setContentType(contentType);
        }
        oss().putObject(config.getBucket(), key, in, meta);
        return key;
    }

    /** 上传 MultipartFile，自动提取 contentType */
    public String upload(MultipartFile file, String key) throws IOException {
        return upload(file.getInputStream(), key, file.getContentType());
    }

    /** 删除 OSS 对象 */
    public void delete(String key) {
        oss().deleteObject(config.getBucket(), key);
    }

    /** 检查 OSS 对象是否存在 */
    public boolean exists(String key) {
        return oss().doesObjectExist(config.getBucket(), key);
    }

    /** 获取 OSS 对象，用于下载或读取 */
    public OSSObject getObject(String key) {
        return oss().getObject(config.getBucket(), key);
    }

    // ==================== URL 生成 ====================

    /**
     * 生成带签名的访问 URL，自动替换为 CDN 域名。
     *
     * @param expireSeconds 签名有效期（秒）
     */
    public String getAccessUrl(String key, long expireSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
        URL signed = oss().generatePresignedUrl(config.getBucket(), key, expiration);
        return replaceDomain(signed.toString());
    }

    /**
     * 生成带签名 + 图片处理参数的访问 URL。
     * 处理参数以 x-oss-process=image/{style} 追加到 URL 末尾。
     *
     * @param expireSeconds 签名有效期（秒）
     * @param style         图片处理样式，如 "resize,w_200" 或 "crop,w_200,h_200"
     */
    public String getAccessUrl(String key, long expireSeconds, String style) {
        String url = getAccessUrl(key, expireSeconds);
        if (style == null || style.isEmpty()) {
            return url;
        }
        String connector = url.contains("?") ? "&" : "?";
        return url + connector + "x-oss-process=image/" + style;
    }

    /** 生成带样式的访问 URL，默认 30 分钟过期 */
    public String getStyledUrl(String key, String style) {
        return getAccessUrl(key, 1800, style);
    }

    /** 生成访问 URL，默认 30 分钟过期 */
    public String getAccessUrl(String key) {
        return getAccessUrl(key, 1800);
    }

    /**
     * 将 OSS 原始域名替换为 CDN 域名。
     * 解析签名 URL → 提取 path+query → 拼接 CDN 域名前缀。
     * 替换失败时降级返回原始 OSS URL。
     */
    private String replaceDomain(String ossUrl) {
        String cdn = config.getCdnDomain();
        if (cdn == null || cdn.isEmpty()) {
            return ossUrl;
        }
        try {
            URL url = new URL(ossUrl);
            String pathAndQuery = url.getFile(); // /key?Expires=...&Signature=...
            if (cdn.endsWith("/")) {
                cdn = cdn.substring(0, cdn.length() - 1);
            }
            return cdn + pathAndQuery;
        } catch (Exception e) {
            log.warn("CDN 域名替换失败, url={}", ossUrl, e);
            return ossUrl;
        }
    }
}
