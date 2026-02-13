package com.qianye.blog.utils;


/**
 * @author: Jintao Cui
 * @desc: 字符串处理工具类
 * @date: 2026/2/13 11:22
 * @version: v1.0
 */
public class StringUtils {

    /**
     * 判断URI是否以指定的字符串开头
     *
     * @param s 待判断的字符串
     * @param nIStartsWithUrls 指定的字符串数组
     * @return 如果URI以指定的字符串开头则返回true，否则返回false
     */
    public static boolean checkStartsWith(String s, String[] nIStartsWithUrls) {
        for (String url : nIStartsWithUrls) {
            if (s.startsWith(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断URI是否包含指定的字符串
     * @param s 待判断的字符串
     * @param unContainsUrls 指定的字符串数组
     * @return 如果URI包含指定的字符串则返回true，否则返回false
     */
    public static boolean checkContains(String s, String[] unContainsUrls) {
        for (String url : unContainsUrls) {
            if (s.contains(url)) {
                return true;
            }
        }
        return false;
    }
}
