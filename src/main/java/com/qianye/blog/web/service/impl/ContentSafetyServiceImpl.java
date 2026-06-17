package com.qianye.blog.web.service.impl;

import com.qianye.blog.web.model.dto.ContentSafetyResult;
import com.qianye.blog.web.service.ContentSafetyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 内容安全检测服务实现
 *
 * @author: Jinto Cui
 * @desc: 初版采用本地词表 + 轻量规则，后续可在此服务内接入云内容审核或人工审核队列。
 * @date: 2026/06/17 23:40
 * @version: v1.0
 */
@Service
@Slf4j
public class ContentSafetyServiceImpl implements ContentSafetyService {

    private static final String WORD_RESOURCE_PATTERN = "classpath*:sensitive-words/*.txt";

    private static final String REJECT_MESSAGE = "评论内容包含不适合发布的内容";

    private static final int MAX_LINK_COUNT = 2;

    private static final int MAX_CONTACT_COUNT = 2;

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?i)(https?://|www\\.)[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    private static final Pattern CONTACT_PATTERN = Pattern.compile(
            "(?i)(微信|vx|v信|qq|电报|telegram|tg|whatsapp|line)\\s*[:：]?\\s*[a-z0-9_\\-]{5,}");

    private static final Pattern REPEAT_CHAR_PATTERN = Pattern.compile("(.)\\1{24,}");

    private static final Pattern REPEAT_PHRASE_PATTERN = Pattern.compile("(.{2,8})\\1{8,}");

    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    private volatile Map<String, List<String>> categoryWords = Collections.emptyMap();

    @PostConstruct
    public void init() {
        this.categoryWords = loadSensitiveWords();
        int total = categoryWords.values().stream().mapToInt(List::size).sum();
        log.info("内容安全本地词表加载完成, categoryCount={}, wordCount={}", categoryWords.size(), total);
    }

    @Override
    public ContentSafetyResult checkComment(String content) {
        return checkText(content, "comment");
    }

    private ContentSafetyResult checkText(String content, String scene) {
        if (StringUtils.isBlank(content)) {
            return ContentSafetyResult.pass();
        }
        String normalized = normalize(content);
        String compact = compactForKeywordMatch(normalized);
        ContentSafetyResult ruleResult = checkRules(normalized);
        if (!ruleResult.isPassed()) {
            log.info("内容安全规则拦截, scene={}, category={}, length={}",
                    scene, ruleResult.getCategory(), content.length());
            return ruleResult;
        }

        for (Map.Entry<String, List<String>> entry : categoryWords.entrySet()) {
            String category = entry.getKey();
            for (String word : entry.getValue()) {
                if (StringUtils.isNotBlank(word) && compact.contains(word)) {
                    log.info("内容安全词表拦截, scene={}, category={}, length={}",
                            scene, category, content.length());
                    return ContentSafetyResult.reject(category, REJECT_MESSAGE);
                }
            }
        }
        return ContentSafetyResult.pass();
    }

    private ContentSafetyResult checkRules(String normalized) {
        if (countMatches(URL_PATTERN, normalized) > MAX_LINK_COUNT) {
            return ContentSafetyResult.reject("ads", "评论包含过多链接，请减少外链后再发布");
        }
        if (countMatches(CONTACT_PATTERN, normalized) > MAX_CONTACT_COUNT) {
            return ContentSafetyResult.reject("ads", "评论包含过多联系方式，请调整后再发布");
        }
        if (REPEAT_CHAR_PATTERN.matcher(normalized).find()
                || REPEAT_PHRASE_PATTERN.matcher(normalized).find()) {
            return ContentSafetyResult.reject("spam", "评论内容重复度过高，请调整后再发布");
        }
        return ContentSafetyResult.pass();
    }

    private int countMatches(Pattern pattern, String text) {
        int count = 0;
        java.util.regex.Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private Map<String, List<String>> loadSensitiveWords() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        try {
            Resource[] resources = resourceResolver.getResources(WORD_RESOURCE_PATTERN);
            for (Resource resource : resources) {
                String category = parseCategory(resource);
                List<String> words = readWords(resource);
                if (!words.isEmpty()) {
                    result.put(category, words);
                }
            }
        } catch (Exception e) {
            log.error("内容安全本地词表加载失败", e);
        }
        return result;
    }

    private String parseCategory(Resource resource) {
        String filename = resource.getFilename();
        if (StringUtils.isBlank(filename)) {
            return "unknown";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    private List<String> readWords(Resource resource) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .filter(line -> !line.startsWith("#"))
                    .map(this::normalize)
                    .map(this::compactForKeywordMatch)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            log.warn("内容安全词表读取失败, file={}", resource.getFilename(), e);
            return Collections.emptyList();
        }
    }

    private String normalize(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 65281 && c <= 65374) {
                builder.append((char) (c - 65248));
            } else if (c == 12288) {
                builder.append(' ');
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    private String compactForKeywordMatch(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
