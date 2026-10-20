package cl.duoc.dsy1107.pedidos360;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
public class MonitoreoConsumer {
  @RabbitListener(queues=RabbitConfig.MONITOREO)
  public void observar(Message msg, Channel channel) throws IOException {
    System.out.println("[TOPIC] routing="+msg.getMessageProperties().getReceivedRoutingKey()+" -> "+new String(msg.getBody(),java.nio.charset.StandardCharsets.UTF_8));
    channel.basicAck(msg.getMessageProperties().getDeliveryTag(),false);
  }
}
