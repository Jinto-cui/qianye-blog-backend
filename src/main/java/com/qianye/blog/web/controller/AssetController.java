package com.qianye.blog.web.controller;

import com.qianye.blog.oss.OssClient;
import com.qianye.blog.web.model.entity.Post;
import com.qianye.blog.web.model.entity.PostAsset;
import com.qianye.blog.web.service.PostAssetService;
import com.qianye.blog.web.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 公开正文资源访问接口。
 *
 * @author: Jinto Cui
 * @desc: 将稳定资源地址 /rest/v1/assets/{id} 重定向到当前有效 OSS 签名 URL
 * @date: 2026/06/06 11:58
 * @version: v1.0
 */
@RestController
@RequestMapping("/rest/v1/assets")
public class AssetController {

    @Autowired
    private PostAssetService postAssetService;
    @Autowired
    private PostService postService;
    @Autowired
    private OssClient ossClient;

    /**
     * 访问正文图片。公开文章资源直接访问，草稿资源仅允许携带匹配 draftToken 预览。
     */
    @GetMapping("/{id}")
    public ResponseEntity<Void> redirect(@PathVariable Long id,
                                         @RequestParam(required = false) String draftToken) {
        PostAsset asset = postAssetService.getById(id);
        if (asset == null) {
            return ResponseEntity.notFound().build();
        }
        if ("draft".equals(asset.getStatus()) && asset.getPostId() == null
                && draftToken != null && draftToken.equals(asset.getDraftToken())) {
            String url = ossClient.getAccessUrl(asset.getObjectKey(), 3600);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
        }
        if (!"active".equals(asset.getStatus()) || asset.getPostId() == null) {
            return ResponseEntity.notFound().build();
        }
        Post post = postService.getById(asset.getPostId());
        if (post == null || post.getPublishedAt() == null) {
            return ResponseEntity.notFound().build();
        }
        String url = ossClient.getAccessUrl(asset.getObjectKey(), 3600);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
