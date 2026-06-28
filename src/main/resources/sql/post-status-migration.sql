-- 2026-06-28 文章发布状态字段增量脚本
-- 适用场景：已有 post 表从 published_at 单字段发布语义升级为 status + published_at。

ALTER TABLE `post`
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '文章状态：draft / published / offline'
        AFTER `mood`;

UPDATE `post`
SET `status` = CASE
    WHEN `published_at` IS NULL THEN 'draft'
    ELSE 'published'
END
WHERE `deleted` = 0;

ALTER TABLE `post`
    ADD KEY `idx_post_status_published_at` (`status`, `published_at`);
