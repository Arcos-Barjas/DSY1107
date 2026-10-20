#!/usr/bin/env bash
set -euo pipefail
python3 "$(dirname "$0")/aplicar_politica.py"
docker exec pedidos360-rabbit1 rabbitmqctl list_policies
