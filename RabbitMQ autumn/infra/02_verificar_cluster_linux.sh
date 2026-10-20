#!/usr/bin/env bash
set -euo pipefail
docker compose -f infra/docker-compose.cluster.yml ps
docker exec pedidos360-rabbit1 rabbitmqctl cluster_status
docker exec pedidos360-rabbit2 rabbitmqctl cluster_status
