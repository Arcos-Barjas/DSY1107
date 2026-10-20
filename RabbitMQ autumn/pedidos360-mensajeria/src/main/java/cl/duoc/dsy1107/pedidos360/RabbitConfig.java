package cl.duoc.dsy1107.pedidos360;
import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitConfig {
  public static final String DIRECT = "pedidos360.direct.exchange";
  public static final String TOPIC = "pedidos360.topic.exchange";
  public static final String RETRY = "pedidos360.retry.exchange";
  public static final String DLX = "pedidos360.dlx";
  public static final String MONITOREO = "pedidos360.monitoreo.queue";
  public static final String[] DOMINIOS = {"notificaciones", "cocina", "documentos"};
  public static String principal(String dominio){ return "pedidos360." + dominio + ".queue"; }
  public static String reintento(String dominio){ return "pedidos360." + dominio + ".retry"; }
  public static String dlq(String dominio){ return "pedidos360." + dominio + ".dlq"; }
  @Bean
  public Declarables mensajeriaDeclarables(){
    DirectExchange main = new DirectExchange(DIRECT, true, false);
    TopicExchange topic = new TopicExchange(TOPIC, true, false);
    DirectExchange retry = new DirectExchange(RETRY, true, false);
    DirectExchange dead = new DirectExchange(DLX, true, false);
    List<Declarable> elements = new ArrayList<>();
    elements.add(main); elements.add(topic); elements.add(retry); elements.add(dead);
    Queue monitor = QueueBuilder.durable(MONITOREO).build();
    elements.add(monitor);
    elements.add(BindingBuilder.bind(monitor).to(topic).with("pedido.*"));
    for(String dominio: DOMINIOS){
      Queue q = QueueBuilder.durable(principal(dominio))
        .deadLetterExchange(DLX).deadLetterRoutingKey(dominio+".failed").build();
      Queue qRetry = QueueBuilder.durable(reintento(dominio))
        .ttl(5000).deadLetterExchange(DIRECT).deadLetterRoutingKey(dominio).build();
      Queue qDead = QueueBuilder.durable(dlq(dominio)).build();
      elements.add(q);
      elements.add(BindingBuilder.bind(q).to(main).with(dominio));
      elements.add(qRetry);
      elements.add(BindingBuilder.bind(qRetry).to(retry).with(dominio));
      elements.add(qDead);
      elements.add(BindingBuilder.bind(qDead).to(dead).with(dominio+".failed"));
    }
    return new Declarables(elements);
  }
}
