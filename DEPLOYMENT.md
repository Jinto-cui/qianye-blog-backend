# 测试环境部署与发布说明

> 适用于浅夜博客前后端分离部署。本文覆盖本地测试环境、类生产环境发布和上线前检查。

## 1. 服务拓扑

| 服务 | 技术 | 默认端口 | 仓库 |
|---|---|---|---|
| 后端 API | Spring Boot 2.7 + MyBatis-Plus | 8080 | `qianye-blog-backend` |
| 前端站点 | Next.js 16 (App Router) | 3000 | `qianye-blog-fronted` |
| 数据库 | MySQL 8.x | 3306 | 外部实例 |

前端通过 `NEXT_PUBLIC_API_BASE_URL` 访问后端；浏览器端只携带 Sa-Token Header，不直连数据库。
`/rest/v1/assets/:path*` 由 Next.js rewrite 到后端，用于正文图片稳定地址。

## 2. 前置条件

- JDK 11
- Maven 3.8+
- Node.js 20+（当前验证版本 v25）
- 可访问的 MySQL 8.x
- 可写的 OSS bucket（正文图片、头像、站点图片）

## 3. 数据库初始化

新库按顺序执行：

```bash
cd qianye-blog-backend
mysql -h <host> -u <user> -p <db> < src/main/resources/sql/table.sql
```

存量库按需执行增量迁移：

```bash
mysql ... < src/main/resources/sql/post-status-migration.sql
mysql ... < src/main/resources/sql/super-admin-role-migration.sql
mysql ... < src/main/resources/sql/site-config-about-seo-migration.sql
```

迁移脚本可重复执行前请先确认列是否已存在；`site-config-about-seo-migration.sql` 使用普通
`ALTER TABLE`，重复执行会报 duplicate column，属预期行为。

## 4. 后端配置与启动

配置文件：`src/main/resources/application-test.yml`（当前默认 profile 为 `test`）。

至少确认：

```yaml
spring:
  datasource:
    url: jdbc:mysql://<host>:3306/<db>?characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: <user>
    password: <password>
oss:
  endpoint: <oss-endpoint>
  access-key-id: <access-key-id>
  access-key-secret: <access-key-secret>
  bucket: <bucket>
app:
  frontend-base-url: https://<frontend-domain>
```

> `application-test.yml` 已被 `.gitignore` 忽略，仓库里的示例值不是可用的生产配置。
> 生产环境建议用环境变量或外部配置中心覆盖数据库、OSS 密钥，不要把真实密钥提交到仓库。

构建与启动：

```bash
cd qianye-blog-backend
JAVA_HOME=/path/to/jdk11 mvn clean package -DskipTests
java -jar target/qianye_blog_backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
```

健康检查：

```bash
curl -s http://127.0.0.1:8080/rest/v1/health
# {"code":20000,"message":"ok","data":{"status":"ok"}}
```

## 5. 前端配置与启动

环境变量（`.env.local` 或部署平台环境变量）：

```bash
NEXT_PUBLIC_API_BASE_URL=http://127.0.0.1:8080
NEXT_PUBLIC_SITE_URL=http://localhost:3000
```

- `NEXT_PUBLIC_API_BASE_URL`：浏览器和服务端访问后端的地址
- `NEXT_PUBLIC_SITE_URL`：站点对外地址，用于 metadataBase、sitemap、robots 和 RSS

构建与启动：

```bash
cd qianye-blog-fronted
npm ci
npm run build
npm run start -- -p 3000
```

生产构建会预渲染大部分页面；`/blog/[slug]`、`/newsletters/[id]`、`/confirm/[token]`
为动态路由，首次访问时由 Node 进程渲染。

## 6. 本地测试环境一键启停

后端仓库 `scripts/` 提供脚本（需要在 monorepo 根目录执行，两个子仓库位于同一父目录）：

```bash
cd <monorepo-root>
# 构建并启动后端 8080 + 前端 3000
./qianye-blog-backend/scripts/start-test-env.sh

# 只启动，不重新构建
./qianye-blog-backend/scripts/start-test-env.sh --no-build

# 停止
./qianye-blog-backend/scripts/stop-test-env.sh
```

脚本会在根目录 `.run/` 下写入 PID 和日志：

```text
.run/backend.pid
.run/frontend.pid
.run/backend.log
.run/frontend.log
```

启动后会等待 `/rest/v1/health` 和 `http://127.0.0.1:3000` 可访问再返回。

## 7. 冒烟验收

```bash
# 后端
curl -s http://127.0.0.1:8080/rest/v1/health
curl -s 'http://127.0.0.1:8080/rest/v1/posts?limit=1&offset=0'
curl -s http://127.0.0.1:8080/rest/v1/guestbook
curl -s http://127.0.0.1:8080/rest/v1/subscribe/count

# 前端
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/blog
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/projects
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/guestbook
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:3000/about
curl -s http://127.0.0.1:3000/sitemap.xml | head
curl -s http://127.0.0.1:3000/feed.xml | head
```

后台验收需要先在数据库把目标账号 `role` 设为 `1`（管理员）或 `2`（超级管理员），登录后访问
`/admin`。普通用户访问 `/rest/v1/admin/**` 返回 `40101`。

## 8. 生产发布建议

1. Nginx 反向代理 3000 和 8080，统一 HTTPS；只暴露 80/443。
2. 前端 `NEXT_PUBLIC_SITE_URL` 使用正式域名，重新构建后 sitemap/robots/RSS 才会使用新域名。
3. 后端 CORS 白名单当前写死 `http://localhost:3000`，上线前改为正式域名或通过配置注入。
4. 邮件服务当前为日志占位（`LogMailServiceImpl`），上线前接入 SMTP/Resend 并实现真实确认与退订链接投递。
5. 限流为单机内存实现，多实例部署时替换为 Redis 计数器。
6. 数据库与 OSS 密钥使用环境变量注入，禁止提交真实密钥；定期备份 MySQL。
7. 进程守护建议使用 systemd 或容器编排，日志接入统一采集。
