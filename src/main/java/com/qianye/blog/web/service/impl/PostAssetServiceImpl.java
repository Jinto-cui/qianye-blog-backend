package com.qianye.blog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianye.blog.common.constant.ErrorCode;
import com.qianye.blog.common.exception.GlobalException;
import com.qianye.blog.oss.OssClient;
import com.qianye.blog.oss.OssKeyGen;
import com.qianye.blog.web.mapper.PostAssetMapper;
import com.qianye.blog.web.model.dto.PostAssetUploadDto;
import com.qianye.blog.web.model.entity.Post;
import com.qianye.blog.web.model.entity.PostAsset;
import com.qianye.blog.web.service.PostAssetService;
import com.qianye.blog.web.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文章正文资源服务实现。
 *
 * @author: Jinto Cui
 * @desc: 实现正文图片上传、草稿资源绑定、正文引用同步和资源归属校验
 * @date: 2026/06/06 11:39
 * @version: v1.0
 */
@Service
@Slf4j
public class PostAssetServiceImpl extends ServiceImpl<PostAssetMapper, PostAsset>
        implements PostAssetService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024L * 1024L;
    private static final Pattern ASSET_URL_PATTERN = Pattern.compile("/rest/v1/assets/(\\d+)");
    private static final Pattern DRAFT_TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{16,64}$");
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>();
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>();

    static {
        ALLOWED_MIME_TYPES.add("image/jpeg");
        ALLOWED_MIME_TYPES.add("image/png");
        ALLOWED_MIME_TYPES.add("image/webp");
        ALLOWED_MIME_TYPES.add("image/gif");
        ALLOWED_EXTENSIONS.add("jpg");
        ALLOWED_EXTENSIONS.add("jpeg");
        ALLOWED_EXTENSIONS.add("png");
        ALLOWED_EXTENSIONS.add("webp");
        ALLOWED_EXTENSIONS.add("gif");
    }

    @Autowired
    private OssClient ossClient;
    @Autowired
    private PostService postService;

    @Override
    public PostAssetUploadDto uploadBodyImage(MultipartFile file, Long postId, String draftToken, Long loginUserId)
            throws IOException {
        validateUploadInput(file, draftToken);
        if (postId != null && postService.getById(postId) == null) {
            throw new GlobalException(ErrorCode.NULL_ERROR, "文章不存在");
        }

        ImageInfo imageInfo = readImageInfo(file);
        String key = OssKeyGen.postBodyImage(file.getOriginalFilename());
        ossClient.upload(file, key);

        PostAsset asset = new PostAsset();
        asset.setPostId(postId);
        asset.setDraftToken(draftToken);
        asset.setObjectKey(key);
        asset.setOriginalName(file.getOriginalFilename());
        asset.setMimeType(file.getContentType());
        asset.setFileSize(file.getSize());
        asset.setWidth(imageInfo.width);
        asset.setHeight(imageInfo.height);
        asset.setUsageType("body");
        asset.setStatus(postId == null ? "draft" : "active");
        asset.setCreatedBy(loginUserId);
        save(asset);

        log.info("正文图片上传成功, assetId={}, postId={}, status={}, userId={}",
                asset.getId(), postId, asset.getStatus(), loginUserId);
        return toUploadDto(asset);
    }

    @Override
    public void bindDraftAssets(Long postId, String draftToken, String markdownBody, Long loginUserId) {
        syncReferencedAssets(postId, draftToken, markdownBody, loginUserId);
    }

    @Override
    public void syncReferencedAssets(Long postId, String draftToken, String markdownBody, Long loginUserId) {
        Set<Long> referencedIds = extractAssetIds(markdownBody);
        List<PostAsset> currentAssets = list(new QueryWrapper<PostAsset>().eq("post_id", postId));
        for (PostAsset asset : currentAssets) {
            if (referencedIds.contains(asset.getId())) {
                if (!"active".equals(asset.getStatus())) {
                    asset.setStatus("active");
                    updateById(asset);
                }
            } else if ("active".equals(asset.getStatus())) {
                asset.setStatus("unused");
                updateById(asset);
            }
        }

        if (referencedIds.isEmpty()) {
            log.info("文章正文资源同步完成, postId={}, activeCount=0", postId);
            return;
        }

        for (Long assetId : referencedIds) {
            PostAsset asset = getById(assetId);
            if (asset == null) {
                throw new GlobalException(ErrorCode.PARAMS_ERROR, "正文引用的图片资源不存在");
            }
            if (postId.equals(asset.getPostId())) {
                continue;
            }
            if (isOwnDraftAsset(asset, draftToken, loginUserId)) {
                asset.setPostId(postId);
                asset.setStatus("active");
                updateById(asset);
                continue;
            }
            throw new GlobalException(ErrorCode.NO_AUTH, "正文引用的图片资源无权绑定到当前文章");
        }

        log.info("文章正文资源同步完成, postId={}, referencedCount={}", postId, referencedIds.size());
    }

    private void validateUploadInput(MultipartFile file, String draftToken) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "图片大小不能超过 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "仅支持 JPG、PNG、WEBP、GIF 图片");
        }
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "图片扩展名不合法");
        }
        if (!StringUtils.hasText(draftToken) || !DRAFT_TOKEN_PATTERN.matcher(draftToken).matches()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "draftToken 格式不合法");
        }
    }

    private ImageInfo readImageInfo(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                return new ImageInfo(image.getWidth(), image.getHeight());
            }
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return new ImageInfo(null, null);
        }
        throw new GlobalException(ErrorCode.PARAMS_ERROR, "图片内容无法识别");
    }

    private Set<Long> extractAssetIds(String markdownBody) {
        Set<Long> result = new HashSet<>();
        if (!StringUtils.hasText(markdownBody)) {
            return result;
        }
        Matcher matcher = ASSET_URL_PATTERN.matcher(markdownBody);
        while (matcher.find()) {
            result.add(Long.valueOf(matcher.group(1)));
        }
        return result;
    }

    private boolean isOwnDraftAsset(PostAsset asset, String draftToken, Long loginUserId) {
        return "draft".equals(asset.getStatus())
                && draftToken != null
                && draftToken.equals(asset.getDraftToken())
                && loginUserId != null
                && loginUserId.equals(asset.getCreatedBy())
                && asset.getPostId() == null;
    }

    private PostAssetUploadDto toUploadDto(PostAsset asset) {
        PostAssetUploadDto dto = new PostAssetUploadDto();
        dto.setId(asset.getId());
        dto.setKey(asset.getObjectKey());
        dto.setRenderUrl("/rest/v1/assets/" + asset.getId());
        dto.setPreviewUrl(dto.getRenderUrl() + "?draftToken=" + asset.getDraftToken());
        dto.setMimeType(asset.getMimeType());
        dto.setSize(asset.getFileSize());
        dto.setWidth(asset.getWidth());
        dto.setHeight(asset.getHeight());
        return dto;
    }

    private String extension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(index + 1).toLowerCase() : "";
    }

    private static class ImageInfo {
        private final Integer width;
        private final Integer height;

        private ImageInfo(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }
    }
}
