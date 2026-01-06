#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib.sh"

need_cmd kubectl
need_cmd docker

if [[ "$WITH_DATA" != "true" ]]; then
  info "skip data (WITH_DATA=false)"
  exit 0
fi

# Kafka image (필요하면)
build_kafka_image
import_kafka_image_to_k3d

apply_manifest_if_exists "$MANIFEST_01"

if [[ "$DO_PORT_FORWARD" == "true" ]]; then
  wait_for_svc "$NS_DATA" "redis" 90 || true
  wait_for_svc "$NS_DATA" "kafka" 90 || true

  if kubectl get ns "$NS_DATA" >/dev/null 2>&1; then
    start_port_forward "$NS_DATA" "redis" "6379:6379" \
      "${PF_DIR}/pf-redis.log" "${PF_DIR}/pf-redis.pid"
    start_port_forward "$NS_DATA" "kafka" "29092:29092" \
      "${PF_DIR}/pf-kafka.log" "${PF_DIR}/pf-kafka.pid"
  else
    warn "data namespace not found: $NS_DATA (skip data port-forward)"
  fi
else
  info "skip port-forward (DO_PORT_FORWARD=false)"
fi