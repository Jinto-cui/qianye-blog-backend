package com.qianye.blog.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.qianye.blog.common.Result;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.oss.OssKeyGen;
import com.qianye.blog.web.model.dto.OssUploadDto;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OSS 对象存储接口，提供服务端上传和删除能力。
 *
 * @author: Jinto Cui
 * @desc: OSS 上传/删除接口
 * @date: 2026/05/24 17:00
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/rest/v1/oss")
public class OssController {

    @Autowired
    private OssClient ossClient;

    /**
     * 上传文件到 OSS。
     * 根据 type 参数按不同目录规则生成 key，返回 key + 签名 URL。
     *
     * @param file 上传文件
     * @param type 资源类型：post | avatar | project | site，默认 temp
     */
    @SaCheckLogin
    @PostMapping("/upload")
    public Result<OssUploadDto> upload(@RequestParam MultipartFile file,
                                       @RequestParam(defaultValue = "temp") String type) throws IOException {
        long userId = StpUtil.getLoginIdAsLong();
        String key = generateKey(type, userId, file.getOriginalFilename());
        ossClient.upload(file, key);
        String url = ossClient.getAccessUrl(key);
        log.info("OSS 上传成功, type={}, key={}", type, key);
        return ResultUtils.success(new OssUploadDto(key, url));
    }

    /** 管理员删除 OSS 对象 */
    @SaCheckRole("admin")
    @DeleteMapping("/object")
    public Result<Void> delete(@RequestParam String key) {
        ossClient.delete(key);
        log.info("OSS 删除成功, key={}", key);
        return ResultUtils.success(null);
    }

    /** 按资源类型分发 key 生成策略 */
    private String generateKey(String type, long userId, String fileName) {
        switch (type) {
            case "post":
                return OssKeyGen.postImage(fileName);
            case "avatar":
                return OssKeyGen.avatar(userId, fileName);
            case "project":
                return OssKeyGen.projectIcon(fileName);
            case "site":
                return OssKeyGen.siteAsset(fileName);
            default:
                return OssKeyGen.temp(fileName);
        }
    }
}
