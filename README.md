# qianye-blog-backend

一个基于 Spring Boot + MyBatis-Plus 的博客后端服务，提供文章、评论、分类、留言、站点配置、用户登录等接口。

## 技术栈

- Java 11
- Spring Boot 2.7.17
- MyBatis / MyBatis-Plus
- MySQL
- Maven

## 主要功能

- 用户：注册、登录、登出、当前用户信息、管理员检索/删除用户
- 文章：文章列表、Slug 列表、文章详情、浏览量递增、评论列表与新增
- 文章资源：后台正文图片上传到 OSS，正文保存稳定 `/rest/v1/assets/{id}` 地址
- 站点内容：站点设置、留言板、分类、订阅用户、简报
- 互动数据：文章浏览量、文章表情反馈（clap / heart / fire / thumbs_up）
- 通用能力：统一返回结构、全局异常处理、请求日志与请求 ID

## 环境要求

- JDK 11+
- Maven 3.8+
- MySQL 8.x（或兼容版本）

## 本地启动

1. 创建数据库并初始化表结构：

```sql
source src/main/resources/sql/table.sql;
```

2. 修改数据库配置（测试环境）：

- 文件：`src/main/resources/application-test.yml`
- 至少需要调整：
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`

3. 启动服务：

```bash
mvn spring-boot:run
```

默认端口：`8080`

## 打包运行

```bash
mvn clean package -DskipTests
java -jar target/qianye_blog_backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
```

## 配置说明

- 主配置：`src/main/resources/application.yml`
- 测试环境配置：`src/main/resources/application-test.yml`
- 当前默认 profile：`test`
- 文件上传限制：
  - `max-file-size: 10MB`
  - `max-request-size: 12MB`
- CORS 当前仅放行 `http://localhost:3000` 到 `/rest/v1/**`

## 接口前缀与分组

项目接口统一使用 `/rest/v1/**` 前缀：

- 公开内容接口：`/rest/v1/posts`、`/rest/v1/site/config`、`/rest/v1/guestbook`
- 用户接口：`/rest/v1/user/**`
- 管理后台接口：`/rest/v1/admin/**`
- 公开资源接口：`/rest/v1/assets/**`
- 旧基础 CRUD 临时兼容接口：`/rest/v1/admin/legacy/**`，全部要求 admin 角色

常用接口示例：

- `GET /rest/v1/posts`
- `GET /rest/v1/posts/{slug}`
- `GET /rest/v1/assets/{id}`
- `POST /rest/v1/posts/{id}/views/incr`
- `GET /rest/v1/posts/{id}/reactions`
- `PATCH /rest/v1/posts/{id}/reactions?index=0`
- `GET /rest/v1/posts/{id}/comments`
- `POST /rest/v1/posts/{id}/comments`
- `GET /rest/v1/site/config`
- `POST /rest/v1/user/register`
- `POST /rest/v1/user/login`
- `GET /rest/v1/user/current`
- `POST /rest/v1/admin/post-assets/upload`

正文图片资源说明：

- 后台编辑器粘贴图片调用 `POST /rest/v1/admin/post-assets/upload`，请求为 `multipart/form-data`，参数为 `file`、`draftToken`、可选 `postId`。
- 上传接口返回 `renderUrl = /rest/v1/assets/{id}` 和草稿预览用 `previewUrl = /rest/v1/assets/{id}?draftToken=...`，文章保存前会把正文规范化为不带查询参数的稳定地址。
- `GET /rest/v1/assets/{id}` 是公开接口，只对 `active + 已绑定文章 + 文章已发布` 的资源返回 302 到 OSS 签名 URL；草稿资源仅允许携带匹配 `draftToken` 预览，未绑定且无 token、未发布文章资源返回 404。
- 创建/更新后台文章时会在事务内同步分类和正文资源引用，避免资源状态与文章内容不一致。

## 统一返回结构

所有接口统一包装为 `Result<T>`：

```json
{
  "code": 0,
  "message": "success",
  "description": "",
  "data": {}
}
```

## 项目结构

```text
src/main/java/com/qianye/blog
├── common        # 通用返回、错误码、异常处理
├── config        # MyBatis-Plus、Web CORS 配置
├── filter        # 请求过滤、日志、requestId 注入
├── utils         # 工具类
└── web
    ├── controller
    ├── mapper
    ├── model
    └── service

src/main/resources
├── application.yml
├── application-test.yml
├── mapper
└── sql/table.sql
```
