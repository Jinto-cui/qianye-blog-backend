package com.qianye.blog.filter;

import com.alibaba.fastjson.JSON;
import com.qianye.blog.common.Result;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 写接口内存限流过滤器
 *
 * @author: Jinto Cui
 * @desc: 对评论、留言、反应、订阅、登录注册等写接口按 IP + 接口组做固定窗口限流；
 *        当前为单机内存实现，后续可替换为 Redis 计数器以支持多实例部署。
 * @date: 2026/11/19 23:40
 * @version: v1.0
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter implements Filter {

    /** 窗口内允许的请求数，达到上限后返回 429。 */
    private static final ConcurrentMap<String, Window> WINDOWS = new ConcurrentHashMap<>();

    /** 内存窗口上限，超过后触发过期清理，避免长期运行无限增长。 */
    private static final int MAX_WINDOW_SIZE = 10000;

    /** 需要限流的接口规则。 */
    private static final List<Rule> RULES = Arrays.asList(
            new Rule("POST", "^/rest/v1/posts/\\d+/comments$", "comment", 5, TimeUnit.MINUTES.toMillis(1)),
            new Rule("POST", "^/rest/v1/guestbook$", "guestbook", 3, TimeUnit.MINUTES.toMillis(1)),
            new Rule("PATCH", "^/rest/v1/posts/\\d+/reactions$", "reaction", 20, TimeUnit.MINUTES.toMillis(1)),
            new Rule("POST", "^/rest/v1/subscribe$", "subscribe", 3, TimeUnit.MINUTES.toMillis(5)),
            new Rule("POST", "^/rest/v1/user/register$", "register", 5, TimeUnit.MINUTES.toMillis(5)),
            new Rule("POST", "^/rest/v1/user/login$", "login", 10, TimeUnit.MINUTES.toMillis(1))
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        Rule rule = matchRule(request);
        if (rule != null) {
            String ip = resolveClientIp(request);
            String key = rule.getName() + ":" + ip;
            long now = System.currentTimeMillis();
            Window window = WINDOWS.computeIfAbsent(key, ignored -> new Window(now));
            synchronized (window) {
                if (now - window.getStartAt() >= rule.getWindowMillis()) {
                    window.setStartAt(now);
                    window.setCount(1);
                } else if (window.getCount() >= rule.getMaxCount()) {
                    log.warn("接口限流触发, rule={}, ip={}, window={}ms, limit={}",
                            rule.getName(), maskIp(ip), rule.getWindowMillis(), rule.getMaxCount());
                    writeRateLimited(response);
                    return;
                } else {
                    window.setCount(window.getCount() + 1);
                }
            }
            cleanupExpired(now);
        }

        chain.doFilter(req, res);
    }

    /**
     * 匹配当前请求对应的限流规则，未命中返回 null。
     */
    private Rule matchRule(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        for (Rule rule : RULES) {
            if (rule.getMethod().equalsIgnoreCase(method) && uri.matches(rule.getPathPattern())) {
                return rule;
            }
        }
        return null;
    }

    /**
     * 解析客户端 IP，优先代理头，最后回退 remoteAddr。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            int comma = forwarded.indexOf(',');
            return (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.trim().isEmpty() ? "unknown" : remote.trim();
    }

    /**
     * 清理过期窗口，避免内存无限增长。
     */
    private void cleanupExpired(long now) {
        if (WINDOWS.size() < MAX_WINDOW_SIZE) {
            return;
        }
        WINDOWS.entrySet().removeIf(entry -> now - entry.getValue().getStartAt() >= TimeUnit.MINUTES.toMillis(10));
    }

    private void writeRateLimited(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = ResultUtils.error(40000, "请求过于频繁，请稍后再试", "");
        response.getWriter().write(JSON.toJSONString(result));
    }

    private String maskIp(String ip) {
        if (ip == null || ip.length() <= 8) {
            return ip;
        }
        return ip.substring(0, 8) + "***";
    }

    /**
     * 单条限流规则。
     */
    private static class Rule {
        private final String method;
        private final String pathPattern;
        private final String name;
        private final int maxCount;
        private final long windowMillis;

        Rule(String method, String pathPattern, String name, int maxCount, long windowMillis) {
            this.method = method;
            this.pathPattern = pathPattern;
            this.name = name;
            this.maxCount = maxCount;
            this.windowMillis = windowMillis;
        }

        String getMethod() {
            return method;
        }

        String getPathPattern() {
            return pathPattern;
        }

        String getName() {
            return name;
        }

        int getMaxCount() {
            return maxCount;
        }

        long getWindowMillis() {
            return windowMillis;
        }
    }

    /**
     * 固定窗口计数。
     */
    private static class Window {
        private long startAt;
        private int count;

        Window(long startAt) {
            this.startAt = startAt;
            this.count = 0;
        }

        long getStartAt() {
            return startAt;
        }

        void setStartAt(long startAt) {
            this.startAt = startAt;
        }

        int getCount() {
            return count;
        }

        void setCount(int count) {
            this.count = count;
        }
    }
}
