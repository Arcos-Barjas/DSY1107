package cl.duoc.dsy1107.pedidos360;
public record PedidoEvento(String eventoId, Long pedidoId, String clienteEmail, String tipo,
     String fecha, boolean falloNotificaciones, boolean falloCocina, boolean falloDocumentos) {}
