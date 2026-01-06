#!/usr/bin/env bash
set -euo pipefail
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib.sh"

need_cmd kubectl
need_cmd docker

if [[ "$WITH_SPARK" != "true" ]]; then
  info "skip spark (WITH_SPARK=false)"
  exit 0
fi

# namespace 보장
kubectl create namespace "$NS_AGG" --dry-run=client -o yaml | kubectl apply -f -

# 이미지 빌드/임포트
build_spark_image
import_spark_image_to_k3d

# 배포
if [[ "$DO_DEPLOY_SPARK" != "true" ]]; then
  info "skip deploy spark (DO_DEPLOY_SPARK=false)"
  exit 0
fi

if [[ -f "$SPARK_MANIFEST" ]]; then
  info "apply spark manifest: $SPARK_MANIFEST"
  kubectl apply -f "$SPARK_MANIFEST"
else
  warn "spark manifest not found (skip): $SPARK_MANIFEST"
  exit 0
fi

# 태그를 매번 바꾸는 구조면, set image가 가장 확실하게 반영됨
if [[ "$FORCE_SET_IMAGE" == "true" ]]; then
  info "force set image: deploy/recipe-trending recipe-trending=$SPARK_IMAGE"
  kubectl -n "$NS_AGG" set image deploy/recipe-trending recipe-trending="$SPARK_IMAGE" --record || true
fi

if [[ "$FORCE_ROLLOUT_RESTART" == "true" ]]; then
  info "force rollout restart: deploy/recipe-trending"
  kubectl -n "$NS_AGG" rollout restart deploy/recipe-trending
fi

info "waiting spark deployment rollout (best-effort)"
kubectl -n "$NS_AGG" rollout status deploy/recipe-trending --timeout=120s || true