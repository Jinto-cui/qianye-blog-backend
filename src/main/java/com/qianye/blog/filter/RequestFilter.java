package com.qianye.blog.filter;


import com.alibaba.fastjson.JSON;
import com.qianye.blog.utils.ContextUtils;
import com.qianye.blog.utils.IPUtils;
import com.qianye.blog.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author: Jinto Cui
 * @desc: http请求过滤器
 * @date: 2025/12/9 23:30
 * @version: v1.0
 */
@Component
@Order(0)
@Slf4j
public class RequestFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    private static final String[] unStartUrls = {"/healthcheck"};

    private static final String[] unContainsUrls = {"/favicon.ico","/static/", "/WEB-INF/","/video/transcode/notify"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpRequest.setCharacterEncoding("utf-8");
        httpResponse.setCharacterEncoding("utf-8");
        if ("OPTIONS".equals(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return ;
        }
        // 记录访问日志
        String URI =  httpRequest.getRequestURI();
        URI = URI.replaceAll("//", "/");
        if(!"/".equals(URI) && !StringUtils.checkStartsWith(URI, unStartUrls) &&
                !StringUtils.checkContains(URI, unContainsUrls)){
            Map<String, String[]> param = httpRequest.getParameterMap();
            String ip = IPUtils.getIp(httpRequest);
            if (!"/actuator/health".equals(URI)) {
                log.info(JSON.toJSONString("[IP:" + ip + "] URI:" + URI + " 上传参数 ： " + JSON.toJSONString(param)));
            }
            ContextUtils.setIp(ip);
        }

        // 获取请求ID
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put("requestId", requestId);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
            ContextUtils.remove();
        }

    }
}
