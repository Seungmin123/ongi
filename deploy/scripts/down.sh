#!/usr/bin/env bash
set -euo pipefail

PF_DIR="${PF_DIR:-/tmp}"
PIDFILES=(
  "${PF_DIR}/pf-otel.pid"
  "${PF_DIR}/pf-grafana.pid"
  "${PF_DIR}/pf-redis.pid"
  "${PF_DIR}/pf-kafka.pid"
)

kill_pidfile() {
  local pidfile="$1"
  if [[ -f "$pidfile" ]]; then
    local pid
    pid="$(cat "$pidfile" || true)"
    if [[ -n "${pid:-}" ]] && ps -p "$pid" >/dev/null 2>&1; then
      echo "killing pid=$pid"
      kill "$pid" || true
    fi
    rm -f "$pidfile"
  fi
}

for pf in "${PIDFILES[@]}"; do
  kill_pidfile "$pf"
done

# 안전망 (pidfile 꼬일 때)
pkill -f "kubectl.*port-forward svc/otel-collector" >/dev/null 2>&1 || true
pkill -f "kubectl.*port-forward svc/grafana" >/dev/null 2>&1 || true
pkill -f "kubectl.*port-forward svc/redis" >/dev/null 2>&1 || true
pkill -f "kubectl.*port-forward svc/kafka" >/dev/null 2>&1 || true

echo "DONE"