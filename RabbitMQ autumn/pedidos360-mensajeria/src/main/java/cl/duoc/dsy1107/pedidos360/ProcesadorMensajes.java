package cl.duoc.dsy1107.pedidos360;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
public class ProcesadorMensajes {
  private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajes.class);
  private static final int REINTENTOS_MAX = 2; // Primera entrega + dos reintentos (total 3).
  private final RabbitTemplate rabbit;
  private final ObjectMapper mapper;
  public ProcesadorMensajes(RabbitTemplate rabbit,ObjectMapper mapper){this.rabbit=rabbit;this.mapper=mapper;}
  public void procesar(String dominio, Message mensaje, Channel ch) throws IOException {
    long tag=mensaje.getMessageProperties().getDeliveryTag();
    Object h=mensaje.getMessageProperties().getHeaders().get("x-lab-intentos");
    int intentos= h instanceof Number n ? n.intValue() : 0;
    try {
      PedidoEvento evento=mapper.readValue(mensaje.getBody(),PedidoEvento.class);
      boolean forzarFallo=switch(dominio){
        case "notificaciones" -> evento.falloNotificaciones();
        case "cocina" -> evento.falloCocina();
        case "documentos" -> evento.falloDocumentos();
        default -> false;
      };
      if (forzarFallo) throw new IllegalStateException("Fallo controlado: "+dominio);
      log.info("[{}] PEDIDO {} procesado, intento={}, redelivered={}",
        dominio, evento.pedidoId(),intentos,mensaje.getMessageProperties().isRedelivered());
      ch.basicAck(tag,false);
    } catch (Exception error){
      log.warn("[{}] Error de procesamiento, intento={}, detalle={}",dominio,intentos,error.getMessage());
      if(intentos<REINTENTOS_MAX){
        try {
          MessageProperties props=new MessageProperties();
          props.setContentType("application/json");
          props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
          props.setHeader("x-lab-intentos",intentos+1);
          props.setHeader("x-lab-origen",dominio);
          rabbit.send(RabbitConfig.RETRY,dominio,new Message(mensaje.getBody(),props));
          log.info("[{}] Enviado a cola de reintento con TTL=5s, numero de reintento={}",dominio,intentos+1);
          ch.basicAck(tag,false); // Ack del original SOLO después del envío de laboratorio.
        } catch(Exception falloDePublicacion){
          log.error("[{}] No se pudo publicar reintento; requeue del original: {}",dominio,falloDePublicacion.getMessage());
          ch.basicNack(tag,false,true);
        }
      } else {
        log.error("[{}] Intentos agotados: basicNack(requeue=false), enrutar a su DLQ",dominio);
        ch.basicNack(tag,false,false); // La cola principal posee x-dead-letter-exchange y routing key.
      }
    }
  }
}
