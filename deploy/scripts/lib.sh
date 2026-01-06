#!/usr/bin/env bash
set -euo pipefail

# -------------------------------
# Defaults (override via env)
CLUSTER_NAME="${CLUSTER_NAME:-ongi}"

# repo root 계산
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Namespaces
NS_DATA="${NS_DATA:-ongi-data}"
NS_OBS="${NS_OBS:-ongi-obs}"
NS_AGG="${NS_AGG:-ongi-agg}"

PF_DIR="${PF_DIR:-/tmp}"

# Manifests
MANIFEST_01="${MANIFEST_01:-k8s/01-data.yaml}"
MANIFEST_02="${MANIFEST_02:-k8s/02-obs.yaml}"
MANIFEST_03="${MANIFEST_03:-k8s/03-ingress.yaml}"
SPARK_MANIFEST="${SPARK_MANIFEST:-k8s/10-spark-trending.yaml}"

# Kafka image
KAFKA_IMAGE_NAME="${KAFKA_IMAGE_NAME:-ongi-kafka}"
KAFKA_IMAGE_TAG="${KAFKA_IMAGE_TAG:-4.1.1-jmx}"
KAFKA_IMAGE="${KAFKA_IMAGE_NAME}:${KAFKA_IMAGE_TAG}"
KAFKA_DOCKERFILE="${KAFKA_DOCKERFILE:-ongi/kafka/Dockerfile}"
KAFKA_BUILD_CONTEXT="${KAFKA_BUILD_CONTEXT:-ongi}"

# Spark image
SPARK_IMAGE_NAME="${SPARK_IMAGE_NAME:-ongi-spark-trending}"
SPARK_IMAGE_TAG="${SPARK_IMAGE_TAG:-$(date +%y%m%d-%H%M%S)}"
SPARK_IMAGE="${SPARK_IMAGE_NAME}:${SPARK_IMAGE_TAG}"
SPARK_DOCKERFILE="${SPARK_DOCKERFILE:-aggregate-domain/Dockerfile.spark}"
SPARK_BUILD_CONTEXT="${SPARK_BUILD_CONTEXT:-aggregate-domain}"
GRADLEW="${GRADLEW:-$REPO_ROOT/gradlew}"

# Component toggles (all.sh 에서 사용)
WITH_CLUSTER="${WITH_CLUSTER:-true}"
WITH_INGRESS="${WITH_INGRESS:-true}"
WITH_DATA="${WITH_DATA:-true}"
WITH_OBS="${WITH_OBS:-true}"
WITH_SPARK="${WITH_SPARK:-true}"

# Fine-grained toggles
DO_APPLY_MANIFESTS="${DO_APPLY_MANIFESTS:-true}"
DO_PORT_FORWARD="${DO_PORT_FORWARD:-true}"

DO_BUILD_KAFKA_IMAGE="${DO_BUILD_KAFKA_IMAGE:-true}"
DO_IMPORT_KAFKA_IMAGE="${DO_IMPORT_KAFKA_IMAGE:-true}"

DO_BUILD_SPARK_IMAGE="${DO_BUILD_SPARK_IMAGE:-true}"
DO_IMPORT_SPARK_IMAGE="${DO_IMPORT_SPARK_IMAGE:-true}"
DO_DEPLOY_SPARK="${DO_DEPLOY_SPARK:-true}"

# Deployment behavior
FORCE_ROLLOUT_RESTART="${FORCE_ROLLOUT_RESTART:-false}"  # true면 deploy restart로 강제 재기동
FORCE_SET_IMAGE="${FORCE_SET_IMAGE:-true}"               # true면 kubectl set image로 태그 반영(강추)

# -------------------------------
info() { echo "▶ $*"; }
warn() { echo "⚠ $*" >&2; }
die() { echo "❌ $*" >&2; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "missing command: $1"
}

cluster_exists() {
  k3d cluster list | awk 'NR>1 {print $1}' | grep -qx "$CLUSTER_NAME"
}

wait_for_svc() {
  local ns="$1"
  local svc="$2"
  local timeout="${3:-60}"

  info "waiting for svc: $ns/$svc (timeout ${timeout}s)"
  local start; start=$(date +%s)

  while true; do
    if kubectl -n "$ns" get svc "$svc" >/dev/null 2>&1; then
      info "svc ready: $ns/$svc"
      return 0
    fi
    local now; now=$(date +%s)
    if (( now - start > timeout )); then
      warn "timeout waiting for svc: $ns/$svc"
      return 1
    fi
    sleep 2
  done
}

start_port_forward() {
  local ns="$1"
  local svc="$2"
  local ports="$3"
  local log="$4"
  local pidfile="$5"

  if [[ -f "$pidfile" ]]; then
    local pid; pid="$(cat "$pidfile" || true)"
    if [[ -n "${pid:-}" ]] && ps -p "$pid" >/dev/null 2>&1; then
      info "port-forward already running (pid=$pid): $ns/$svc $ports"
      return 0
    fi
  fi

  pkill -f "kubectl.*-n ${ns} port-forward svc/${svc}" >/dev/null 2>&1 || true

  info "starting port-forward: $ns/$svc $ports"
  nohup kubectl -n "$ns" port-forward "svc/${svc}" $ports >"$log" 2>&1 &
  echo $! > "$pidfile"
  info "port-forward started (pid=$(cat "$pidfile")), log=$log"
}

build_kafka_image() {
  [[ "$DO_BUILD_KAFKA_IMAGE" == "true" ]] || { info "skip kafka image build"; return 0; }

  info "checking local kafka image: $KAFKA_IMAGE"
  if docker image inspect "$KAFKA_IMAGE" >/dev/null 2>&1; then
    info "local image exists: $KAFKA_IMAGE (skip build)"
    return 0
  fi

  info "building kafka image: $KAFKA_IMAGE"
  docker build -t "$KAFKA_IMAGE" -f "$KAFKA_DOCKERFILE" "$KAFKA_BUILD_CONTEXT"
}

import_kafka_image_to_k3d() {
  [[ "$DO_IMPORT_KAFKA_IMAGE" == "true" ]] || { info "skip kafka image import"; return 0; }
  info "importing kafka image into k3d cluster: $CLUSTER_NAME"
  k3d image import "$KAFKA_IMAGE" -c "$CLUSTER_NAME"
}

build_spark_image() {
  [[ "$DO_BUILD_SPARK_IMAGE" == "true" ]] || { info "skip spark image build"; return 0; }

  info "building docker artifacts (prepareDocker) for aggregate-domain"
  ( cd "$REPO_ROOT" && "$GRADLEW" :aggregate-domain:prepareDocker )

  info "building spark image: $SPARK_IMAGE"
  docker build --no-cache -t "$SPARK_IMAGE" -f "$REPO_ROOT/$SPARK_DOCKERFILE" "$REPO_ROOT/$SPARK_BUILD_CONTEXT"
}

import_spark_image_to_k3d() {
  [[ "$DO_IMPORT_SPARK_IMAGE" == "true" ]] || { info "skip spark image import"; return 0; }
  info "importing spark image into k3d cluster: $CLUSTER_NAME"
  k3d image import "$SPARK_IMAGE" -c "$CLUSTER_NAME"
}

apply_manifest_if_exists() {
  local f="$1"
  [[ "$DO_APPLY_MANIFESTS" == "true" ]] || { info "skip apply manifests"; return 0; }

  if [[ -f "$f" ]]; then
    info "apply manifest: $f"
    kubectl apply -f "$f"
  else
    warn "manifest not found (skip): $f"
  fi
}