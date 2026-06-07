package com.qianye.blog.web.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.qianye.blog.common.Result;
import com.qianye.blog.utils.ResultUtils;
import com.qianye.blog.web.model.dto.PostAssetUploadDto;
import com.qianye.blog.web.service.PostAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 管理后台文章正文资源上传。
 *
 * @author: Jinto Cui
 * @desc: 接收编辑器粘贴/拖拽图片，上传 OSS 后返回稳定 Markdown 资源地址
 * @date: 2026/06/06 11:55
 * @version: v1.0
 */
@Slf4j
@RestController
@RequestMapping("/rest/v1/admin/post-assets")
public class AdminPostAssetController {

    @Autowired
    private PostAssetService postAssetService;

    /**
     * 上传文章正文图片。
     *
     * @param file       图片文件
     * @param postId     已有文章 ID，编辑文章时传入
     * @param draftToken 草稿 token，新建和编辑文章时均传入
     */
    @PostMapping("/upload")
    public Result<PostAssetUploadDto> upload(@RequestParam MultipartFile file,
                                             @RequestParam(required = false) Long postId,
                                             @RequestParam String draftToken) throws IOException {
        long loginUserId = StpUtil.getLoginIdAsLong();
        PostAssetUploadDto dto = postAssetService.uploadBodyImage(file, postId, draftToken, loginUserId);
        log.info("后台正文图片上传完成, assetId={}, userId={}", dto.getId(), loginUserId);
        return ResultUtils.success(dto);
    }
}
