package cl.duoc.dsy1107.pedidos360;
public record PedidoSolicitud(Long pedidoId, String clienteEmail,
      boolean falloNotificaciones, boolean falloCocina, boolean falloDocumentos) {}
