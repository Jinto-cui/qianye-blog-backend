#!/usr/bin/env bash
#
# 本地测试环境停止脚本
#
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="$ROOT/.run"

log() { printf '\033[1;32m==>\033[0m %s\n' "$*"; }

stop_one() {
  local name="$1" pid_file="$RUN_DIR/$1.pid"
  if [[ ! -f "$pid_file" ]]; then
    log "$name 未运行（缺少 ${pid_file}）"
    return
  fi
  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" >/dev/null 2>&1; then
    kill "$pid" >/dev/null 2>&1 || true
    for _ in {1..15}; do
      kill -0 "$pid" >/dev/null 2>&1 || break
      sleep 1
    done
    if kill -0 "$pid" >/dev/null 2>&1; then
      kill -9 "$pid" >/dev/null 2>&1 || true
    fi
    log "$name 已停止（pid=${pid}）"
  else
    log "$name 进程不存在（pid=${pid}）"
  fi
  rm -f "$pid_file"
}

stop_one frontend
stop_one backend

log "测试环境已停止"
