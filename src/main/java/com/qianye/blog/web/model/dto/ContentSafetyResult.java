package com.qianye.blog.web.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 内容安全检测结果
 *
 * @author: Jinto Cui
 * @desc: 仅暴露类别和通用提示，不返回具体命中词，避免日志和响应二次扩散敏感内容。
 * @date: 2026/06/17 23:40
 * @version: v1.0
 */
@Data
public class ContentSafetyResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否通过检测 */
    private boolean passed;

    /** 命中类别 */
    private String category;

    /** 可返回给用户的通用提示 */
    private String message;

    public static ContentSafetyResult pass() {
        ContentSafetyResult result = new ContentSafetyResult();
        result.setPassed(true);
        return result;
    }

    public static ContentSafetyResult reject(String category, String message) {
        ContentSafetyResult result = new ContentSafetyResult();
        result.setPassed(false);
        result.setCategory(category);
        result.setMessage(message);
        return result;
    }
}
