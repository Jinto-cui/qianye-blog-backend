package com.qianye.blog.filter.wrapper;


import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author: Jinto Cui
 * @desc: HTTP响应封装类
 * @date: 2025/12/10 00:06
 * @version: v1.0
 */
public class ResponseWrapper extends HttpServletResponseWrapper {
    private final HttpServletResponse originalResponse;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        this.originalResponse = response;
    }

    @Override
    public void setHeader(String name, String value) {
        // 确保只在响应未提交时设置
        if (!isCommitted()) {
            originalResponse.setHeader(name, value);
        }
    }

    @Override
    public boolean isCommitted() {
        return originalResponse.isCommitted();
    }
}