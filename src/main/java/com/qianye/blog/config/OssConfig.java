package com.qianye.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OSS 配置属性，绑定 application.yml 中 oss.* 配置项。
 * 生产环境 AK/SK 通过环境变量注入，不写死在配置文件。
 *
 * @author: Jinto Cui
 * @desc: 阿里云 OSS 配置属性类
 * @date: 2026/05/24 17:00
 * @version: v1.0
 */
@Data
@Component
@ConfigurationProperties("oss")
public class OssConfig {
    /** OSS endpoint，如 oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;
    /** AccessKey ID，通过环境变量 OSS_ACCESS_KEY_ID 注入 */
    private String accessKeyId;
    /** AccessKey Secret，通过环境变量 OSS_ACCESS_KEY_SECRET 注入 */
    private String accessKeySecret;
    /** Bucket 名称，dev/prod 通过 Spring profile 区分 */
    private String bucket;
    /** CDN 加速域名（可选），配置后签名 URL 自动替换为 CDN 域名 */
    private String cdnDomain;
}
