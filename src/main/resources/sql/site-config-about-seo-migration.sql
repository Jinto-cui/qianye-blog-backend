-- 站点配置补充：SEO 标题/描述与关于页个人介绍
-- 适用：已执行过旧版 table.sql 的存量数据库
ALTER TABLE `site_config`
    ADD COLUMN `site_title` VARCHAR(128) NULL COMMENT '站点标题（SEO / 关于页）' AFTER `oss_base_url`,
    ADD COLUMN `site_description` VARCHAR(255) NULL COMMENT '站点描述（SEO）' AFTER `site_title`,
    ADD COLUMN `about_content` MEDIUMTEXT NULL COMMENT '关于页个人介绍 Markdown' AFTER `site_description`;
