package com.qianye.blog.web.controller.admin;

import com.qianye.blog.common.Result;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.oss.OssKeyGen;
import com.qianye.blog.web.model.dto.OssUploadDto;
import com.qianye.blog.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 管理后台 — OSS 文件上传
 *
 * @author: Jinto Cui
 * @desc: admin 权限上传，受 SaTokenConfig 拦截器保护
 * @date: 2026/05/25 14:00
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/rest/v1/admin")
public class AdminUploadController {

    @Autowired
    private OssClient ossClient;

    /**
     * 上传文件到 OSS，返回 key + 签名 URL 供前端预览。
     *
     * @param file 上传文件
     * @param type 资源类型：site | post | avatar | project，默认 site
     */
    @PostMapping("/upload")
    public Result<OssUploadDto> upload(@RequestParam MultipartFile file,
                                       @RequestParam(defaultValue = "site") String type) throws IOException {
        String key;
        switch (type) {
            case "post":
                key = OssKeyGen.postImage(file.getOriginalFilename());
                break;
            case "avatar":
                key = OssKeyGen.avatar(0L, file.getOriginalFilename());
                break;
            case "project":
                key = OssKeyGen.projectIcon(file.getOriginalFilename());
                break;
            default:
                key = OssKeyGen.siteAsset(file.getOriginalFilename());
        }
        ossClient.upload(file, key);
        String url = ossClient.getAccessUrl(key);
        log.info("Admin OSS 上传成功, type={}, key={}", type, key);
        return ResultUtils.success(new OssUploadDto(key, url));
    }
}
