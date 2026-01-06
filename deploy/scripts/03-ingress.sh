#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib.sh"

need_cmd kubectl
need_cmd helm

if [[ "$WITH_INGRESS" != "true" ]]; then
  info "skip ingress (WITH_INGRESS=false)"
  exit 0
fi

info "helm repo add/update: ingress-nginx"
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx >/dev/null 2>&1 || true
helm repo update >/dev/null

info "install/upgrade ingress-nginx controller"
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx --create-namespace

apply_manifest_if_exists "$MANIFEST_03"