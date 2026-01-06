#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib.sh"

need_cmd k3d
need_cmd kubectl

if [[ "$WITH_CLUSTER" != "true" ]]; then
  info "skip cluster (WITH_CLUSTER=false)"
  exit 0
fi

info "checking k3d cluster: $CLUSTER_NAME"
if cluster_exists; then
  info "cluster exists: $CLUSTER_NAME"
else
  info "creating cluster: $CLUSTER_NAME"
  k3d cluster create "$CLUSTER_NAME" \
    --servers 1 --agents 2 \
    -p "80:80@loadbalancer" \
    -p "443:443@loadbalancer" \
    --k3s-arg "--disable=traefik@server:0"
fi

info "kubectl connectivity check"
kubectl cluster-info >/dev/null