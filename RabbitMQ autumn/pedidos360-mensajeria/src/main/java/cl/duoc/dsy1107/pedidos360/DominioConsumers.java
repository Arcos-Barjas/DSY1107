package cl.duoc.dsy1107.pedidos360;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
public class DominioConsumers {
  private final ProcesadorMensajes processor;
  public DominioConsumers(ProcesadorMensajes processor){this.processor=processor;}
  @RabbitListener(queues="pedidos360.notificaciones.queue")
  public void notificaciones(Message msg, Channel ch) throws IOException {processor.procesar("notificaciones",msg,ch);}
  @RabbitListener(queues="pedidos360.cocina.queue")
  public void cocina(Message msg, Channel ch) throws IOException {processor.procesar("cocina",msg,ch);}
  @RabbitListener(queues="pedidos360.documentos.queue")
  public void documentos(Message msg, Channel ch) throws IOException {processor.procesar("documentos",msg,ch);}
}
