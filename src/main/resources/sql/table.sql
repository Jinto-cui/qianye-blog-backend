-- ============================================================
-- 博客系统完整 DDL
-- 版本：v2.0（去 Sanity 化，统一字段规范）
-- 规范：所有表统一 created_at / updated_at / deleted(TINYINT UNSIGNED)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. user — 用户（Phase 0）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user`
(
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_account   VARCHAR(64)      NOT NULL COMMENT '登录账号',
    user_password  VARCHAR(128)     NOT NULL COMMENT '密码（BCrypt）',
    nickname       VARCHAR(64)      NULL     COMMENT '昵称 / 展示名',
    email          VARCHAR(128)     NULL     COMMENT '邮箱',
    avatar_key     VARCHAR(512)     NULL     COMMENT '头像 OSS object key',
    bio            VARCHAR(256)     NULL     COMMENT '个人简介',
    social_links   JSON             NULL     COMMENT '社交链接 [{ "platform": "github", "url": "..." }]',
    role           TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '角色：0 = 普通用户，1 = 管理员',
    status         TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0 = 正常，1 = 停用',
    last_login_at  DATETIME         NULL     COMMENT '最后登录时间',
    last_login_ip  VARCHAR(64)      NULL     COMMENT '最后登录 IP',
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account (user_account),
    UNIQUE KEY uk_user_email (email),
    KEY idx_user_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户';


-- ============================================================
-- 2. category — 文章分类（Phase 1）
-- ============================================================
-- 设计理由：
--   name 而非 title：分类是名称不是标题，语义更准确
--   slug 独立字段：Sanity slug 是对象，这里直接存字符串
--   sort_order：控制前端展示顺序
CREATE TABLE IF NOT EXISTS `category`
(
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(64)      NOT NULL COMMENT '分类名称（如：前端 / 后端 / 设计）',
    slug        VARCHAR(128)     NOT NULL COMMENT 'URL 标识符',
    description VARCHAR(256)     NULL     COMMENT '简要描述',
    sort_order  INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '排序权重（越大越靠前）',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name),
    UNIQUE KEY uk_category_slug (slug),
    KEY idx_category_sort (sort_order)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文章分类';


-- ============================================================
-- 3. post — 文章（Phase 1）
-- ============================================================
-- 设计理由：
--   body 替代 body_json：Markdown 替代 Portable Text JSON
--   主图字段扁平化：四列独立字段替代嵌套 image 对象
--   mood VARCHAR(16) 替代 ENUM：避免 ALTER TABLE 锁表
--   author_id 关联 user：规范的外键关联
CREATE TABLE IF NOT EXISTS `post`
(
    id                     BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    title                  VARCHAR(200)     NOT NULL COMMENT '文章标题',
    slug                   VARCHAR(200)     NOT NULL COMMENT 'URL 标识符',
    description            TEXT             NULL     COMMENT '摘要（列表页展示）',
    body                   MEDIUMTEXT       NULL     COMMENT '正文（Markdown 格式）',
    mood                   VARCHAR(16)      NOT NULL DEFAULT 'neutral' COMMENT '情绪：neutral / happy / sad',
    published_at           DATETIME         NULL     COMMENT '发布时间（NULL 表示草稿）',
    reading_time           INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '阅读时长（分钟）',
    main_image_key         VARCHAR(512)     NULL     COMMENT '主图 OSS object key',
    main_image_lqip        MEDIUMTEXT       NULL     COMMENT '主图 LQIP（base64 缩略图）',
    main_image_dominant_bg CHAR(7)          NULL     COMMENT '主图主色背景（#RRGGBB）',
    main_image_dominant_fg CHAR(7)          NULL     COMMENT '主图主色前景（#RRGGBB）',
    views                  BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '浏览量',
    author_id              BIGINT UNSIGNED  NULL     COMMENT '作者 ID（关联 user.id）',
    created_at             DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at             DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_slug (slug),
    KEY idx_post_published_at (published_at),
    KEY idx_post_author (author_id),
    KEY idx_post_mood (mood)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文章';


-- ============================================================
-- 4. post_category — 文章-分类关联（Phase 1）
-- ============================================================
-- 设计理由：
--   独立 id 主键：MyBatis-Plus 对单主键支持更好
--   UNIQUE(post_id, category_id)：防止重复关联
CREATE TABLE IF NOT EXISTS `post_category`
(
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_id     BIGINT UNSIGNED  NOT NULL COMMENT '文章 ID',
    category_id BIGINT UNSIGNED  NOT NULL COMMENT '分类 ID',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_category (post_id, category_id),
    KEY idx_pc_category (category_id),
    KEY idx_pc_post (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文章-分类关联';


-- ============================================================
-- 4.1. post_asset — 文章正文资源（后台编辑器上传）
-- ============================================================
-- 设计理由：
--   正文 Markdown 保存稳定资源地址 /rest/v1/assets/{id}，不写 OSS 短签名 URL。
--   新建文章未保存前使用 draft_token + created_by 绑定草稿资源，保存后再绑定 post_id。
--   status 默认 draft，公开访问接口只允许 active 且已绑定已发布文章的资源。
CREATE TABLE IF NOT EXISTS `post_asset`
(
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_id        BIGINT UNSIGNED  NULL     COMMENT '文章 ID，新建草稿阶段为空',
    draft_token    VARCHAR(64)      NULL     COMMENT '前端草稿 token，用于新文章保存前绑定资源',
    object_key     VARCHAR(512)     NOT NULL COMMENT 'OSS object key',
    original_name  VARCHAR(255)     NULL     COMMENT '原始文件名',
    mime_type      VARCHAR(100)     NOT NULL COMMENT 'MIME 类型',
    file_size      BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    width          INT UNSIGNED     NULL     COMMENT '图片宽度',
    height         INT UNSIGNED     NULL     COMMENT '图片高度',
    alt            VARCHAR(255)     NULL     COMMENT '图片替代文本',
    usage_type     VARCHAR(32)      NOT NULL DEFAULT 'body' COMMENT '用途：body 正文图片',
    status         VARCHAR(32)      NOT NULL DEFAULT 'draft' COMMENT '状态：draft / active / unused / deleted',
    created_by     BIGINT UNSIGNED  NULL     COMMENT '上传人用户 ID',
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    KEY idx_post_asset_post (post_id),
    KEY idx_post_asset_draft (draft_token),
    KEY idx_post_asset_created_by (created_by),
    KEY idx_post_asset_object_key (object_key),
    KEY idx_post_asset_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文章正文资源';


-- ============================================================
-- 5. site_config — 站点配置（单行固定列，Phase 1）
-- ============================================================
-- 设计理由：
--   固定列替代键值对：博客站点配置项在开发时明确定义，不会频繁动态增删。
--   固定列方案代码直观，MyBatis-Plus 直接映射，无需手动解析 JSON key。
--   未来如有灵活扩展需求再迁移到键值对，数据量小，迁移成本低。
CREATE TABLE IF NOT EXISTS `site_config`
(
    id           BIGINT UNSIGNED  NOT NULL DEFAULT 1 COMMENT '主键（固定为 1，单行数据）',
    hero_photos  JSON             NULL     COMMENT '首页图片 OSS key 数组 ["posts/hero/1.jpg", ...]',
    resume       JSON             NULL     COMMENT '简历经历 [{ "company", "title", "logoKey", "start", "end" }]',
    social_links JSON             NULL     COMMENT '社交链接 [{ "platform", "url" }]',
    oss_base_url VARCHAR(256)     NULL     COMMENT 'OSS 基础 URL（如 https://oss-cn-hangzhou.aliyuncs.com）',
    created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '站点配置';

INSERT IGNORE INTO `site_config` (id, hero_photos, resume, social_links) VALUES
(1, '[]', '[]', '[]');


-- ============================================================
-- 6. project — 项目展示（Phase 1）
-- ============================================================
-- 设计理由：
--   独立表替代旧 site_setting.projects JSON 嵌套
--   支持独立 CRUD、排序，字段有明确约束
CREATE TABLE IF NOT EXISTS `project`
(
    id          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        VARCHAR(128)     NOT NULL COMMENT '项目名称',
    url         VARCHAR(512)     NULL     COMMENT '项目链接',
    description TEXT             NULL     COMMENT '项目简介',
    icon_key    VARCHAR(512)     NULL     COMMENT '图标 OSS object key',
    sort_order  INT UNSIGNED     NOT NULL DEFAULT 0 COMMENT '排序权重（越大越靠前）',
    created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    KEY idx_project_sort (sort_order)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '项目展示';


-- ============================================================
-- 7. post_reaction — 文章反应（Phase 2）
-- ============================================================
-- 设计理由：
--   用户级记录而非聚合计数表：(post_id, user_id, reaction_type) 联合唯一。
--   旧表只有 post_id + 四个计数列，无法防重复。新表支持去重，COUNT GROUP BY 实时计算。
--   reaction_type VARCHAR 可扩展，无需 ALTER TABLE 加列。
CREATE TABLE IF NOT EXISTS `post_reaction`
(
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_id       BIGINT UNSIGNED  NOT NULL COMMENT '文章 ID',
    user_id       BIGINT UNSIGNED  NOT NULL COMMENT '用户 ID（关联 user.id）',
    reaction_type VARCHAR(16)      NOT NULL COMMENT '反应类型：clap / heart / fire / thumbs_up',
    created_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_reaction_user_post_type (post_id, user_id, reaction_type),
    KEY idx_reaction_post (post_id),
    KEY idx_reaction_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文章反应';


-- ============================================================
-- 8. comment — 文章评论（Phase 3）
-- ============================================================
-- 设计理由：
--   user_id BIGINT 关联 user 表，不再存 user_info JSON。头像/昵称 JOIN 获取，数据一致。
--   body TEXT 存纯文本或 Markdown 片段，旧表 body JSON 存 Portable Text 无必要。
--   parent_id 支持嵌套回复（NULL = 顶级评论）。
CREATE TABLE IF NOT EXISTS `comment`
(
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_id    BIGINT UNSIGNED  NOT NULL COMMENT '文章 ID',
    user_id    BIGINT UNSIGNED  NOT NULL COMMENT '评论用户 ID（关联 user.id）',
    body       TEXT             NOT NULL COMMENT '评论内容',
    parent_id  BIGINT UNSIGNED  NULL     COMMENT '父评论 ID（NULL 表示顶级评论）',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    KEY idx_comment_post (post_id),
    KEY idx_comment_user (user_id),
    KEY idx_comment_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '文章评论';


-- ============================================================
-- 9. guestbook — 留言墙（Phase 4）
-- ============================================================
-- 设计理由：
--   user_id 关联 user 表，不再存 user_info JSON。
--   body 替代旧 message 字段，与 comment 表命名一致。
CREATE TABLE IF NOT EXISTS `guestbook`
(
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT UNSIGNED  NOT NULL COMMENT '留言用户 ID（关联 user.id）',
    body       TEXT             NOT NULL COMMENT '留言内容',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    KEY idx_guestbook_user (user_id),
    KEY idx_guestbook_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '留言墙';


-- ============================================================
-- 10. subscriber — 简报订阅者（Phase 6）
-- ============================================================
-- 设计理由：
--   独立的邮件订阅表，与 user 表解耦。未注册用户也可订阅。
--   token 用于确认邮件中的点击链接，confirmed 标记确认状态。
--   旧 bg_subscribed_user 实际是旧版用户表（含 password/status 等），已由 user 表替代。
CREATE TABLE IF NOT EXISTS `subscriber`
(
    id              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    email           VARCHAR(128)     NOT NULL COMMENT '订阅邮箱',
    token           VARCHAR(128)     NULL     COMMENT '确认 token（UUID）',
    confirmed       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否确认：0 = 待确认，1 = 已确认',
    subscribed_at   DATETIME         NULL     COMMENT '确认时间（正式订阅时间）',
    unsubscribed_at DATETIME         NULL     COMMENT '取消订阅时间',
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_subscriber_email (email),
    KEY idx_subscriber_confirmed (confirmed)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '简报订阅者';


-- ============================================================
-- 11. newsletter — 简报推送记录（Phase 7）
-- ============================================================
-- 设计理由：
--   旧表名 newsletters（复数），修正为单数 newsletter，与其他表命名一致。
--   sent_at 为 NULL 表示草稿，非 NULL 表示已发送。
CREATE TABLE IF NOT EXISTS `newsletter`
(
    id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    subject    VARCHAR(200)     NULL     COMMENT '邮件标题',
    body       MEDIUMTEXT       NULL     COMMENT '邮件正文（HTML）',
    sent_at    DATETIME         NULL     COMMENT '实际发送时间（NULL = 草稿）',
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted    TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 = 未删除，1 = 已删除',
    PRIMARY KEY (id),
    KEY idx_newsletter_sent (sent_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '简报推送记录';
