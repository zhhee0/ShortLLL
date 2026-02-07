#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
TAG=${TAG:-latest}
REGISTRY=${REGISTRY:-}

image() {
  echo "${REGISTRY}${1}:${TAG}"
}

echo "==> Building backend jars"
"${ROOT_DIR}/mvnw" -DskipTests package

echo "==> Building docker images"
docker build -f "${ROOT_DIR}/admin/Dockerfile" -t "$(image shortlink-admin)" "${ROOT_DIR}"
docker build -f "${ROOT_DIR}/project/Dockerfile" -t "$(image shortlink-project)" "${ROOT_DIR}"
docker build -f "${ROOT_DIR}/aggregation/Dockerfile" -t "$(image shortlink-aggregation)" "${ROOT_DIR}"
docker build -f "${ROOT_DIR}/gateway/Dockerfile" -t "$(image shortlink-gateway)" "${ROOT_DIR}"
docker build -f "${ROOT_DIR}/console-vue/Dockerfile" -t "$(image shortlink-console)" "${ROOT_DIR}"

echo "Done. Images:"
echo "  $(image shortlink-admin)"
echo "  $(image shortlink-project)"
echo "  $(image shortlink-aggregation)"
echo "  $(image shortlink-gateway)"
echo "  $(image shortlink-console)"
