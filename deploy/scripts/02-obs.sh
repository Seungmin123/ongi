#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib.sh"

need_cmd kubectl

if [[ "$WITH_OBS" != "true" ]]; then
  info "skip obs (WITH_OBS=false)"
  exit 0
fi

apply_manifest_if_exists "$MANIFEST_02"

if [[ "$DO_PORT_FORWARD" == "true" ]]; then
  wait_for_svc "$NS_OBS" "otel-collector" 90 || true
  wait_for_svc "$NS_OBS" "grafana" 90 || true

  if kubectl get ns "$NS_OBS" >/dev/null 2>&1; then
    start_port_forward "$NS_OBS" "otel-collector" "4317:4317 4318:4318" \
      "${PF_DIR}/pf-otel.log" "${PF_DIR}/pf-otel.pid"
    start_port_forward "$NS_OBS" "grafana" "3000:3000" \
      "${PF_DIR}/pf-grafana.log" "${PF_DIR}/pf-grafana.pid"
  else
    warn "obs namespace not found: $NS_OBS (skip obs port-forward)"
  fi
else
  info "skip port-forward (DO_PORT_FORWARD=false)"
fi