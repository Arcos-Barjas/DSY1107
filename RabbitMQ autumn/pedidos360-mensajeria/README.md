# Pedidos360 - Mensajería RabbitMQ - Semana 9 SOLUCIÓN DOCENTE

Proyecto didáctico incremental Spring Boot 3.3.5 / Java 21 / Spring AMQP.
Mantener esta solución independiente de la aplicación completa EP1: la integración al producto
final debe conservar Angular, MSAL, API Gateway, Spring Security y persistencia cloud.

## Inicio rápido
1. Desde la raíz del paquete: `docker compose up -d` (Semana 09) o seguir la guía de clúster (Semana 10).
2. RabbitMQ UI: http://localhost:15672, usuario `labuser`, clave `labpass` (SOLO desarrollo local).
3. Abrir la carpeta `pedidos360-mensajeria` en IntelliJ o terminal Windows:

```powershell
cd pedidos360-mensajeria
java -version
.\mvnw.cmd -version
.\mvnw.cmd spring-boot:run
```

En macOS/Linux ejecutar `./mvnw spring-boot:run`. Primera ejecución requiere Internet para Maven 3.9.11.
No subir target/, contraseñas reales ni `.env` a GitHub.

## Pruebas REST
POST http://localhost:8080/api/lab/pedidos/aceptar
Content-Type: application/json

```json
{"pedidoId":105,"clienteEmail":"prueba@example.com","falloNotificaciones":false,"falloCocina":false,"falloDocumentos":false}
```

Repetir con `falloNotificaciones:true` para observar reintentos y DLQ (en solución completa).
POST http://localhost:8080/api/lab/eventos/topic?tipo=pedido.aceptado&pedidoId=105

## Lógica docente de reintento (solución)
* Entrega inicial = intento 0; reintento 1 y 2 (3 entregas como máximo).
* Cada error controlado envía nueva copia persistente a la cola `.retry` (TTL 5000 ms);
  luego esa cola devuelve el mensaje a la principal mediante DLX.
* Tras agotar intentos, `basicNack(tag,false,false)` lleva el original a la DLQ del dominio.
* `basicNack(tag,false,true)` solo si falla publicar el reintento para recuperar el original.
* En producción usar Publisher Confirms gestionados, idempotencia y estrategia de entrega transaccional/outbox.
* No editar tipos/durabilidad de colas ya existentes: si cambió su argumento, usar un volumen de laboratorio limpio
  sólo ANTES de probar retención/durabilidad; no usar `down -v` para medir persistencia.

## Indicadores RabbitMQ UI
Exchanges: pedidos360.direct.exchange, pedidos360.topic.exchange, pedidos360.retry.exchange, pedidos360.dlx.
Queues: tres principales + tres `.retry` + tres `.dlq` + monitoreo topic.
