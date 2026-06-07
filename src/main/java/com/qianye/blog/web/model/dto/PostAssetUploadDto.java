package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文章正文图片上传结果。
 *
 * @author: Jinto Cui
 * @desc: 返回稳定渲染地址和图片基础元数据，供编辑器写入 Markdown
 * @date: 2026/06/06 11:34
 * @version: v1.0
 */
@Data
public class PostAssetUploadDto implements Serializable {

    /** 资源主键 */
    private Long id;

    /** OSS object key */
    private String key;

    /** Markdown 正文使用的稳定资源地址 */
    private String renderUrl;

    /** 编辑器草稿阶段预览地址，保存正文前需要规范化为 renderUrl */
    private String previewUrl;

    /** MIME 类型 */
    private String mimeType;

    /** 文件大小，单位字节 */
    private Long size;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    private static final long serialVersionUID = 1L;
}
