package com.qianye.blog.web.interceptor;


import com.qianye.blog.utils.ContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Jintao Cui
 * @desc: 通用拦截器
 * @date: 2026/2/13 11:44
 * @version: v1.0
 */
public class CommonInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求都处理完成，DispatcherServlet渲染了对应的视图。ThreadLocal值清除，防止内存泄漏
        ContextUtils.remove();
    }
}
