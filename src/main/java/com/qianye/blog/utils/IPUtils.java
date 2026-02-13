package com.qianye.blog.utils;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * @author: Jintao Cui
 * @desc: ip相关工具类
 * @date: 2026/2/13 11:27
 * @version: v1.0
 */
@Slf4j
public class IPUtils {

    /**
     * ip请求头
     */
    private static final String IP_HEADER = "x-mfpiexfbdaqhieybp-ip";

    public static String getIp(HttpServletRequest request) {
        return getIpInternal(
                request::getHeader,
                "ServletRequest: "
        );
    }

    /**
     * 通用的IP获取方法
     * @param headerGetter 请求头获取函数
     * @param logPrefix 日志前缀
     * @return 客户端IP地址
     */
    private static String getIpInternal(Function<String, String> headerGetter, String logPrefix) {
        //真实客户端ip请求头
        String ip = headerGetter.apply(IP_HEADER);
        if (ip != null && !ip.trim().isEmpty()) {
            return ip;
        }

        ip = headerGetter.apply("X-Forwarded-For");
        if (ip == null || ip.trim().isEmpty()) {
            ip = headerGetter.apply("X-Real-IP");
        }else{
            log.info(logPrefix + "X-Forwarded-IP: " + ip);
        }
        return ip;
    }

}
