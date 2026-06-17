package com.qianye.blog.web.model.request;

import lombok.Data;

/**
 * 新增文章评论请求
 *
 * @author: Jinto Cui
 * @desc: body 为 Markdown 子集文本，parentId 预留一级回复能力，userInfo 为旧字段兼容占位不参与写入。
 * @date: 2026/06/17 17:30
 * @version: v1.1
 */
@Data
public class CreateCommentRequest {

    /** 旧模板兼容字段，后端不信任也不写入 */
    private String userInfo;

    /** 评论正文 */
    private String body;

    /** 父评论 ID */
    private Long parentId;
}
