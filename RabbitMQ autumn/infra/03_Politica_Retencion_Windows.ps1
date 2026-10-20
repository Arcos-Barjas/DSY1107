# La Management API evita problemas de escape JSON de PowerShell legado.
python "$PSScriptRoot/aplicar_politica.py"
if ($LASTEXITCODE -ne 0) { throw "Error aplicando política. Revise Python/Management API." }
docker exec pedidos360-rabbit1 rabbitmqctl list_policies
