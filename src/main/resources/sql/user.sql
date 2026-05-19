-- ============================================================
-- 用户表 DDL（现代化博客系统）
-- 策略：user 表专注认证与个人身份，订阅/留言/评论数据由对应业务表承载
-- ============================================================

CREATE TABLE IF NOT EXISTS `user`
(
    id             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_account   VARCHAR(64)      NOT NULL COMMENT '登录账号',
    user_password  VARCHAR(128)     NOT NULL COMMENT '密码（BCrypt 加密，长度 ~60，预留余量）',
    nickname       VARCHAR(64)      NULL     COMMENT '昵称 / 展示名',
    email          VARCHAR(128)     NULL     COMMENT '邮箱',
    avatar_key     VARCHAR(512)     NULL     COMMENT '头像 OSS object key',
    bio            VARCHAR(256)     NULL     COMMENT '个人简介（短句）',
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
    KEY idx_user_status (status),
    KEY idx_user_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '用户';

-- ============================================================
-- 字段设计说明
-- ============================================================
-- ✅ 保留：
--   user_account   — 登录凭证，唯一索引
--   user_password  — BCrypt（$2a$...），扩充至 128 以兼容未来算法升级
--   nickname       — 替代原 username，语义更清晰
--   email          — 唯一索引，可用于找回密码 / 订阅通知
--   avatar_key     — 独立字段（不从社交链接 JSON 中提取），高频读取
--   role           — 缩小至 TINYINT，与 Sa-Token StpInterface 对接
--   status         — 停用/正常，配合 Sa-Token 登录时校验
--
-- ✅ 新增：
--   bio            — 个人简介，Profile 页展示
--   social_links   — JSON 数组，每个元素 { platform, url }，可扩展
--   last_login_at  — 审计/安全
--   last_login_ip  — 审计/安全
--   created_at     — 统一所有表的时间字段命名
--   updated_at     — 同上
--   deleted        — 统一所有表的逻辑删除字段命名
--
-- ❌ 移除：
--   code           — 语义不清，id 已能唯一标识用户
--   gender         — 博客场景非必需，需要时可在 social_links 或 bio 中自行声明
--   phone          — 同上；若未来需短信通知，可加回
--   createTime     — 命名不一致，统一为 created_at + updated_at
--   updateTime
--   isDelete       — 命名不一致，统一为 deleted
