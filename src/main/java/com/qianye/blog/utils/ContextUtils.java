package com.qianye.blog.utils;


import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jintao Cui
 * @desc: 基于ThreadLocal的全局上下文工具操作工具类
 * @date: 2026/2/13 11:40
 * @version: v1.0
 */
public class ContextUtils {
    /**
     * ThreadLocal对象
     */
    private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    /**
     * ip
     */
    public static final String CONTEXT_KEY_IP = "ip";

    /**
     * 用户id
     */
    public static final String CONTEXT_KEY_USER_ID = "userId";


    /**
     * 获取数据
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        Map<String, Object> map = threadLocal.get();
        if (map == null) {
            map = new HashMap<>(6);
            threadLocal.set(map);
        }
        return map.get(key);
    }

    /**
     * 设置数据
     * @param key   键
     * @param value 值
     */
    public static void set(String key, Object value) {
        Map<String, Object> map = threadLocal.get();
        if (map == null) {
            map = new HashMap<>(6);
            threadLocal.set(map);
        }
        map.put(key, value);
    }

    /**
     * 清除数据
     */
    public static void remove() {
        threadLocal.remove();
    }

    public static void setIp(String ip) {
        set(CONTEXT_KEY_IP, ip);
    }

}
