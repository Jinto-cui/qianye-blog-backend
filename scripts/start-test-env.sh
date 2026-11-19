#!/usr/bin/env bash
#
# 本地测试环境启动脚本
# 启动后端 8080 + 前端 3000，PID/日志写入仓库根目录 .run/
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="$ROOT/.run"
BACKEND_DIR="$ROOT/qianye-blog-backend"
FRONTEND_DIR="$ROOT/qianye-blog-fronted"
BUILD=1

if [[ "${1:-}" == "--no-build" ]]; then
  BUILD=0
fi

log() { printf '\033[1;32m==>\033[0m %s\n' "$*"; }
fail() { printf '\033[1;31mERROR:\033[0m %s\n' "$*" >&2; exit 1; }

mkdir -p "$RUN_DIR"

port_busy() {
  lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
}

wait_for_url() {
  local url="$1" name="$2" attempts="${3:-60}"
  for ((i = 1; i <= attempts; i++)); do
    if curl -fsS -m 2 -o /dev/null "$url"; then
      log "$name 已就绪: $url"
      return 0
    fi
    sleep 2
  done
  fail "$name 启动超时，请查看 $RUN_DIR/${name}.log"
}

if [[ ! -d "$BACKEND_DIR" || ! -d "$FRONTEND_DIR" ]]; then
  fail "未找到 qianye-blog-backend / qianye-blog-fronted，请把脚本放在 monorepo 根目录"
fi

if port_busy 8080; then
  fail "端口 8080 已被占用，请先停止现有后端"
fi
if port_busy 3000; then
  fail "端口 3000 已被占用，请先停止现有前端"
fi

find_java11() {
  local candidates=()
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
    candidates+=("$JAVA_HOME")
  fi
  local dir
  for dir in "$HOME"/.jenv/versions/11* /Library/Java/JavaVirtualMachines/*/Contents/Home; do
    [[ -d "$dir" ]] && candidates+=("$dir")
  done
  for dir in "${candidates[@]}"; do
    if "$dir/bin/java" -version 2>&1 | grep -q 'version "11'; then
      printf '%s' "$dir"
      return 0
    fi
  done
  return 1
}

JAVA_HOME="$(find_java11)" || fail "未找到 JDK 11，请设置 JAVA_HOME 指向 JDK 11"
export JAVA_HOME
JAVA_CMD="$JAVA_HOME/bin/java"
log "使用 JDK: $JAVA_HOME"

if [[ "$BUILD" == "1" ]]; then
  log "构建后端（mvn clean package -DskipTests）"
  (cd "$BACKEND_DIR" && JAVA_HOME="$JAVA_HOME" mvn -q clean package -DskipTests)

  log "构建前端（npm run build）"
  (cd "$FRONTEND_DIR" && npm run build)
fi

JAR="$BACKEND_DIR/target/qianye_blog_backend-0.0.1-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then
  fail "未找到后端 jar: $JAR，请去掉 --no-build 重新构建"
fi
if [[ ! -f "$FRONTEND_DIR/.next/BUILD_ID" ]]; then
  fail "未找到前端构建产物 .next，请去掉 --no-build 重新构建"
fi

log "启动后端 8080"
(
  cd "$BACKEND_DIR"
  nohup "$JAVA_CMD" -jar "$JAR" --spring.profiles.active=test \
    > "$RUN_DIR/backend.log" 2>&1 &
  echo $! > "$RUN_DIR/backend.pid"
)

wait_for_url "http://127.0.0.1:8080/rest/v1/health" "backend"

log "启动前端 3000"
(
  cd "$FRONTEND_DIR"
  nohup node node_modules/next/dist/bin/next start -p 3000 \
    > "$RUN_DIR/frontend.log" 2>&1 &
  echo $! > "$RUN_DIR/frontend.pid"
)

wait_for_url "http://127.0.0.1:3000/" "frontend"

log "测试环境已启动"
log "前端: http://127.0.0.1:3000"
log "后端: http://127.0.0.1:8080/rest/v1/health"
log "日志: $RUN_DIR/backend.log, $RUN_DIR/frontend.log"
