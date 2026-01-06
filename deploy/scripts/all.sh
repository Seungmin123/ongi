#!/usr/bin/env bash
# WITH_CLUSTER=true
# WITH_INGRESS=true
# WITH_DATA=true
# WITH_OBS=true
# WITH_SPARK=true
# DO_BUILD_SPARK_IMAGE=true
# DO_IMPORT_SPARK_IMAGE=true
# DO_APPLY_MANIFESTS=false
# DO_PORT_FORWARD=true
# FORCE_SET_IMAGE=true
# FORCE_ROLLOUT_RESTART=true
# ./scripts/deploy/all.sh

set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$BASE_DIR/lib.sh"

need_cmd bash

info "=== deploy start ==="
info "WITH_CLUSTER=$WITH_CLUSTER WITH_DATA=$WITH_DATA WITH_OBS=$WITH_OBS WITH_INGRESS=$WITH_INGRESS WITH_SPARK=$WITH_SPARK"
info "DO_APPLY_MANIFESTS=$DO_APPLY_MANIFESTS DO_PORT_FORWARD=$DO_PORT_FORWARD"
info "SPARK_IMAGE=$SPARK_IMAGE"

# 순서 고정(의존성 고려)
bash "$BASE_DIR/00-cluster.sh"
bash "$BASE_DIR/03-ingress.sh"
bash "$BASE_DIR/01-data.sh"
bash "$BASE_DIR/02-obs.sh"
bash "$BASE_DIR/10-spark.sh"

info "DONE"
echo ""
echo "Links/Ports (if DO_PORT_FORWARD=true):"
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