-- 可选：切库与字符集
-- CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- USE blog;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS bg_subscribed_user
(
    id              bigint auto_increment comment '主键'
        primary key,
    user_name       varchar(64) default 'NULL' not null comment '用户名',
    nick_name       varchar(64) default 'NULL' not null comment '昵称',
    password        varchar(64) default 'NULL' not null comment '密码',
    type            char        default '0'    null comment '用户类型：0代表普通用户，1代表管理员',
    status          char        default '0'    null comment '账号状态（0正常 1停用）',
    email           varchar(64)                null comment '邮箱',
    phone_number    varchar(32)                null comment '手机号',
    sex             char                       null comment '用户性别（0男，1女，2未知）',
    avatar          varchar(128)               null comment '头像',
    subscribed_at   DATETIME                   NULL COMMENT '订阅时间',
    unsubscribed_at DATETIME                   NULL COMMENT '取消时间',
    create_by       bigint                     null comment '创建人的用户id',
    create_time     datetime                   null comment '创建时间',
    update_by       bigint                     null comment '更新人',
    update_time     datetime                   null comment '更新时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）'
)
    comment '用户表';

-- posts：文章
CREATE TABLE IF NOT EXISTS bg_post
(
    id              BIGINT UNSIGNED                NOT NULL AUTO_INCREMENT COMMENT '主键',
    ext_id          VARCHAR(64)                    NOT NULL COMMENT '外部ID（前端/_id）',
    slug            VARCHAR(128)                   NOT NULL COMMENT '短链唯一标识',
    title           VARCHAR(200)                   NOT NULL COMMENT '标题',
    description     TEXT                           NULL COMMENT '摘要',
    mood            ENUM ('neutral','happy','sad') NOT NULL DEFAULT 'neutral' COMMENT '情绪',
    published_at    DATETIME                       NOT NULL COMMENT '发布时间',
    reading_time    INT UNSIGNED                   NOT NULL DEFAULT 0 COMMENT '阅读分钟数',
    main_image_url  VARCHAR(512)                   NULL COMMENT '主图URL',
    main_image_lqip MEDIUMTEXT                     NULL COMMENT '主图LQIP',
    main_image_fg   CHAR(7)                        NULL COMMENT '主色前景',
    main_image_bg   CHAR(7)                        NULL COMMENT '主色背景',
    body_json       JSON                           NULL COMMENT '正文JSON（Portable Text）',
    views           BIGINT UNSIGNED                NOT NULL DEFAULT 0 COMMENT '浏览量',
    created_at      DATETIME                       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME                       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_posts_ext_id (ext_id),
    UNIQUE KEY uk_posts_slug (slug),
    KEY idx_posts_published_at (published_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章';

-- categories：分类
CREATE TABLE IF NOT EXISTS bg_category
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    title      VARCHAR(120)    NOT NULL COMMENT '分类名',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_title (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='分类';

-- post_categories：文章-分类关系（多对多）
CREATE TABLE IF NOT EXISTS post_category
(
    post_id     BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (post_id, category_id),
    KEY idx_pc_category (category_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章-分类关系';

-- comments：评论
CREATE TABLE IF NOT EXISTS bg_comment
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_id    BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    user_id    VARCHAR(200)    NOT NULL COMMENT '用户ID',
    user_info  JSON            NULL COMMENT '用户信息JSON',
    body       JSON            NULL COMMENT '评论内容JSON',
    parent_id  BIGINT UNSIGNED NULL COMMENT '父评论ID',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (id),
    KEY idx_comments_post (post_id),
    KEY idx_comments_parent (parent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论';

-- guestbook：客墙
CREATE TABLE IF NOT EXISTS bg_guestbook
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    VARCHAR(200)    NOT NULL COMMENT '用户ID',
    user_info  JSON            NULL COMMENT '用户信息JSON',
    message    TEXT            NOT NULL COMMENT '留言内容',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (id),
    KEY idx_guestbook_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='客墙';

-- post_reactions：表情计数（四类）
CREATE TABLE IF NOT EXISTS post_reaction
(
    post_id   BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    clap      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '👏',
    heart     INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '❤️',
    fire      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '🔥',
    thumbs_up INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '👍',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='表情计数';

-- post_views：浏览量（可与Redis双写或定期落库）
CREATE TABLE IF NOT EXISTS post_view
(
    post_id BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
    views   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (post_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='浏览量';


-- newsletter：简报
CREATE TABLE IF NOT EXISTS newsletters
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    subject    VARCHAR(200)    NULL COMMENT '标题',
    body       MEDIUMTEXT      NULL COMMENT '正文',
    sent_at    DATETIME        NULL COMMENT '发送时间',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='简报';

-- site_setting：站点设置（单行JSON）
CREATE TABLE IF NOT EXISTS site_setting
(
    id          TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '固定ID=1',
    projects    JSON             NULL COMMENT '项目JSON',
    hero_photos JSON             NULL COMMENT '首页图片JSON',
    resume      JSON             NULL COMMENT '简历JSON',
    updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        int         default 0      null comment '删除标志（0代表未删除，1代表已删除）',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='站点设置';