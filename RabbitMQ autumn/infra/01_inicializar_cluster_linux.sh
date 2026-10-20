#!/usr/bin/env bash
set -euo pipefail
docker compose -f infra/docker-compose.cluster.yml up -d
docker exec pedidos360-rabbit1 rabbitmqctl await_startup
docker exec pedidos360-rabbit2 rabbitmqctl await_startup
docker exec pedidos360-rabbit2 rabbitmqctl stop_app
docker exec pedidos360-rabbit2 rabbitmqctl reset
docker exec pedidos360-rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
docker exec pedidos360-rabbit2 rabbitmqctl start_app
docker exec pedidos360-rabbit1 rabbitmqctl cluster_status
