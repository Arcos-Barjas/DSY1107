# Ejecutar desde la raíz de la solución en PowerShell. Requiere Docker Desktop.
$ErrorActionPreference="Stop"
docker compose -f infra/docker-compose.cluster.yml up -d
if ($LASTEXITCODE -ne 0) { throw "No se pudo iniciar Compose" }
Write-Host "Esperando a que RabbitMQ inicie en ambos nodos..."
docker exec pedidos360-rabbit1 rabbitmqctl await_startup
docker exec pedidos360-rabbit2 rabbitmqctl await_startup
Write-Host "Uniendo rabbit2 al clúster rabbit1 (primera ejecución)."
docker exec pedidos360-rabbit2 rabbitmqctl stop_app
docker exec pedidos360-rabbit2 rabbitmqctl reset
docker exec pedidos360-rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
docker exec pedidos360-rabbit2 rabbitmqctl start_app
docker exec pedidos360-rabbit1 rabbitmqctl cluster_status
Write-Host "UI: localhost:15672 / localhost:15673 (labuser/labpass)"
