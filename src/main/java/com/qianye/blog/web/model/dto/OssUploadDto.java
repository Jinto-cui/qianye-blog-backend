package com.qianye.blog.web.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 上传结果 DTO。
 *
 * @author: Jinto Cui
 * @desc: OSS 上传响应体
 * @date: 2026/05/24 17:00
 * @version: v1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadDto {
    /** OSS 对象 key，调用方存入对应实体（如 Post.mainImageKey） */
    private String key;
    /** 完整访问 URL，默认 30 分钟签名有效，前端可直接使用 */
    private String url;
}
