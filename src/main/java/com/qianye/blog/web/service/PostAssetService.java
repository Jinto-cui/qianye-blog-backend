package com.qianye.blog.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.blog.web.model.dto.PostAssetUploadDto;
import com.qianye.blog.web.model.entity.PostAsset;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文章正文资源服务。
 *
 * @author: Jinto Cui
 * @desc: 处理编辑器正文图片上传、草稿绑定、正文引用同步和公开访问校验
 * @date: 2026/06/06 11:35
 * @version: v1.0
 */
public interface PostAssetService extends IService<PostAsset> {

    /**
     * 上传正文图片到 OSS，并写入资源元数据。
     */
    PostAssetUploadDto uploadBodyImage(MultipartFile file, Long postId, String draftToken, Long loginUserId)
            throws IOException;

    /**
     * 创建文章时，将当前管理员草稿资源绑定到真实文章。
     */
    void bindDraftAssets(Long postId, String draftToken, String markdownBody, Long loginUserId);

    /**
     * 更新文章时，同步正文中仍被引用的资源，并标记历史移除资源。
     */
    void syncReferencedAssets(Long postId, String draftToken, String markdownBody, Long loginUserId);
}
