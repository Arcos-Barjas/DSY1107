package cl.duoc.dsy1107.pedidos360;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.UUID;
@Service
public class PedidoPublisher {
  private final RabbitTemplate rabbit;
  private final ObjectMapper mapper;
  public PedidoPublisher(RabbitTemplate rabbit, ObjectMapper mapper){ this.rabbit=rabbit;this.mapper=mapper; }
  public PedidoEvento publicarPedido(PedidoSolicitud req) throws JsonProcessingException {
    PedidoEvento e = new PedidoEvento(UUID.randomUUID().toString(),req.pedidoId(),req.clienteEmail(),
        "PEDIDO_ACEPTADO",OffsetDateTime.now().toString(),req.falloNotificaciones(),req.falloCocina(),req.falloDocumentos());
    String json = mapper.writeValueAsString(e);
    for(String dominio: RabbitConfig.DOMINIOS){
      rabbit.convertAndSend(RabbitConfig.DIRECT,dominio,json,message->{
        message.getMessageProperties().setContentType("application/json");
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return message;
      });
    }
    return e;
  }
  public void publicarEventoTopic(String tipo, Long pedidoId) throws JsonProcessingException {
    // Ejemplos válidos: pedido.aceptado y pedido.cancelado; no incluye información sensible.
    if (!tipo.matches("pedido\\.[a-z]+")) throw new IllegalArgumentException("Usar un tipo como pedido.aceptado");
    String json=mapper.writeValueAsString(java.util.Map.of("tipo",tipo,"pedidoId",pedidoId,
      "fecha",OffsetDateTime.now().toString()));
    rabbit.convertAndSend(RabbitConfig.TOPIC,tipo,json);
  }
}
