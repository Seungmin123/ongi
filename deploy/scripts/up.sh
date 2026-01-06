#!/usr/bin/env bash
set -euo pipefail

CLUSTER_NAME="${CLUSTER_NAME:-ongi}"

# up.sh 위치 기준으로 repo root 계산
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Manifests (순서 중요하면 이 배열 순서대로 적용)
MANIFESTS=(
  "${MANIFEST_01:-k8s/01-data.yaml}"
  "${MANIFEST_02:-k8s/02-obs.yaml}"
  "${MANIFEST_03:-k8s/03-ingress.yaml}"
)

# Namespaces (yaml에 이미 namespace가 정의되어 있어도, 포트포워딩/검증용으로 명시)
NS_DATA="${NS_DATA:-ongi-data}"
NS_OBS="${NS_OBS:-ongi-obs}"

PF_DIR="${PF_DIR:-/tmp}"

# -------------------------------
# Custom Kafka Image (k8s)
KAFKA_IMAGE_NAME="${KAFKA_IMAGE_NAME:-ongi-kafka}"
KAFKA_IMAGE_TAG="${KAFKA_IMAGE_TAG:-4.1.1-jmx}"
KAFKA_IMAGE="${KAFKA_IMAGE_NAME}:${KAFKA_IMAGE_TAG}"

KAFKA_DOCKERFILE="${KAFKA_DOCKERFILE:-ongi/kafka/Dockerfile}"
KAFKA_BUILD_CONTEXT="${KAFKA_BUILD_CONTEXT:-ongi}"

# -------------------------------
# Spark trending job (k8s)
NS_AGG="${NS_AGG:-ongi-agg}"

SPARK_IMAGE_NAME="${SPARK_IMAGE_NAME:-ongi-spark-trending}"
SPARK_IMAGE_TAG="${SPARK_IMAGE_TAG:-$(date +%y%m%d-%H%M%S)}"
SPARK_IMAGE="${SPARK_IMAGE_NAME}:${SPARK_IMAGE_TAG}"

# aggregate-domain fatjar + spark runner image
SPARK_DOCKERFILE="${SPARK_DOCKERFILE:-aggregate-domain/Dockerfile.spark}"
SPARK_BUILD_CONTEXT="${SPARK_BUILD_CONTEXT:-aggregate-domain}"

# spark k8s manifest
SPARK_MANIFEST="${SPARK_MANIFEST:-k8s/10-spark-trending.yaml}"

# gradle wrapper (repo root)
GRADLEW="${GRADLEW:-$REPO_ROOT/gradlew}"

info() { echo "▶ $*"; }
warn() { echo "⚠ $*" >&2; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "❌ missing command: $1" >&2; exit 1; }
}

cluster_exists() {
  k3d cluster list | awk 'NR>1 {print $1}' | grep -qx "$CLUSTER_NAME"
}

build_kafka_image() {
  info "checking local kafka image: $KAFKA_IMAGE"

  if docker image inspect "$KAFKA_IMAGE" >/dev/null 2>&1; then
    info "local image exists: $KAFKA_IMAGE (skip build)"
    return
  fi

  info "building kafka image: $KAFKA_IMAGE"
  docker build \
    -t "$KAFKA_IMAGE" \
    -f "$KAFKA_DOCKERFILE" \
    "$KAFKA_BUILD_CONTEXT"
}

import_kafka_image_to_k3d() {
  info "importing kafka image into k3d cluster: $CLUSTER_NAME"
  k3d image import "$KAFKA_IMAGE" -c "$CLUSTER_NAME"
}

build_spark_image() {
  info "checking local spark image: $SPARK_IMAGE"

#  if docker image inspect "$SPARK_IMAGE" >/dev/null 2>&1; then
#    info "local image exists: $SPARK_IMAGE (skip build)"
#    return
#  fi

  info "building docker artifacts (prepareDocker) for aggregate-domain"
  ( cd "$REPO_ROOT" && "$GRADLEW" :aggregate-domain:prepareDocker )

  info "building spark image: $SPARK_IMAGE"
  docker build --no-cache \
    -t "$SPARK_IMAGE" \
    -f "$REPO_ROOT/$SPARK_DOCKERFILE" \
    "$REPO_ROOT/$SPARK_BUILD_CONTEXT"
}

import_spark_image_to_k3d() {
  info "importing spark image into k3d cluster: $CLUSTER_NAME"
  k3d image import "$SPARK_IMAGE" -c "$CLUSTER_NAME"
}

deploy_spark() {
  # namespace는 manifest에 포함돼 있어도, 안정적으로 한 번 더 보장
  kubectl create namespace "$NS_AGG" --dry-run=client -o yaml | kubectl apply -f -

  if [[ -f "$SPARK_MANIFEST" ]]; then
    info "apply spark manifest: $SPARK_MANIFEST"
    kubectl apply -f "$SPARK_MANIFEST"
  else
    warn "spark manifest not found (skip): $SPARK_MANIFEST"
    return
  fi

  info "waiting spark deployment rollout (best-effort)"
  kubectl -n "$NS_AGG" rollout status deploy/recipe-trending --timeout=120s || true
}

start_port_forward() {
  local ns="$1"
  local svc="$2"
  local ports="$3"
  local log="$4"
  local pidfile="$5"

  # 이미 살아있으면 재사용
  if [[ -f "$pidfile" ]]; then
    local pid
    pid="$(cat "$pidfile" || true)"
    if [[ -n "${pid:-}" ]] && ps -p "$pid" >/dev/null 2>&1; then
      info "port-forward already running (pid=$pid): $ns/$svc $ports"
      return
    fi
  fi

  # 같은 svc port-forward가 이미 떠 있으면 정리(보수적으로)
  pkill -f "kubectl.*-n ${ns} port-forward svc/${svc}" >/dev/null 2>&1 || true

  info "starting port-forward: $ns/$svc $ports"
  nohup kubectl -n "$ns" port-forward "svc/${svc}" $ports >"$log" 2>&1 &
  echo $! > "$pidfile"
  info "port-forward started (pid=$(cat "$pidfile")), log=$log"
}

wait_for_svc() {
  local ns="$1"
  local svc="$2"
  local timeout="${3:-60}"

  info "waiting for svc: $ns/$svc (timeout ${timeout}s)"
  local start
  start=$(date +%s)

  while true; do
    if kubectl -n "$ns" get svc "$svc" >/dev/null 2>&1; then
      info "svc ready: $ns/$svc"
      return
    fi
    local now
    now=$(date +%s)
    if (( now - start > timeout )); then
      warn "timeout waiting for svc: $ns/$svc"
      return
    fi
    sleep 2
  done
}

main() {
  need_cmd k3d
  need_cmd kubectl
  need_cmd helm

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

  build_kafka_image
  import_kafka_image_to_k3d

  info "kubectl connectivity check"
  kubectl cluster-info >/dev/null

  info "helm repo add/update: ingress-nginx"
  helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx >/dev/null 2>&1 || true
  helm repo update >/dev/null

  info "install/upgrade ingress-nginx controller"
  helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
    --namespace ingress-nginx --create-namespace

  # manifests apply
  for f in "${MANIFESTS[@]}"; do
    if [[ -f "$f" ]]; then
      info "apply manifest: $f"
      kubectl apply -f "$f"
    else
      warn "manifest not found (skip): $f"
    fi
  done

  # port-forwards (서비스가 생길 때까지 잠깐 대기)
  wait_for_svc "$NS_OBS" "otel-collector" 90
  wait_for_svc "$NS_OBS" "grafana" 90
  wait_for_svc "$NS_DATA" "redis" 90
  wait_for_svc "$NS_DATA" "kafka" 90

  # Spark Trending Job
  build_spark_image
  import_spark_image_to_k3d
  deploy_spark

  # Obs
  if kubectl get ns "$NS_OBS" >/dev/null 2>&1; then
    start_port_forward "$NS_OBS" "otel-collector" "4317:4317 4318:4318" \
      "${PF_DIR}/pf-otel.log" "${PF_DIR}/pf-otel.pid"
    start_port_forward "$NS_OBS" "grafana" "3000:3000" \
      "${PF_DIR}/pf-grafana.log" "${PF_DIR}/pf-grafana.pid"
  else
    warn "obs namespace not found: $NS_OBS (skip obs port-forward)"
  fi

  # Data
  if kubectl get ns "$NS_DATA" >/dev/null 2>&1; then
    start_port_forward "$NS_DATA" "redis" "6379:6379" \
      "${PF_DIR}/pf-redis.log" "${PF_DIR}/pf-redis.pid"
    start_port_forward "$NS_DATA" "kafka" "29092:29092" \
      "${PF_DIR}/pf-kafka.log" "${PF_DIR}/pf-kafka.pid"
  else
    warn "data namespace not found: $NS_DATA (skip data port-forward)"
  fi

  info "DONE"
  echo ""
  echo "Links/Ports:"
  echo "  Grafana:     http://localhost:3000"
  echo "  OTEL gRPC:   localhost:4317"
  echo "  OTEL HTTP:   localhost:4318"
  echo "  Redis:       localhost:6379"
  echo "  Kafka:       localhost:29092"
  echo ""
  echo "Logs:"
  echo "  ${PF_DIR}/pf-otel.log"
  echo "  ${PF_DIR}/pf-grafana.log"
  echo "  ${PF_DIR}/pf-redis.log"
  echo "  ${PF_DIR}/pf-kafka.log"
}

main "$@"