package cl.duoc.dsy1107.pedidos360;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
@RestController
@RequestMapping("/api/lab")
public class EventoController {
  private final PedidoPublisher producer;
  public EventoController(PedidoPublisher producer){this.producer=producer;}
  @PostMapping("/pedidos/aceptar")
  public ResponseEntity<PedidoEvento> aceptar(@RequestBody PedidoSolicitud solicitud) throws JsonProcessingException {
    if(solicitud.pedidoId()==null || solicitud.pedidoId()<=0) return ResponseEntity.badRequest().build();
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(producer.publicarPedido(solicitud));
  }
  @PostMapping("/eventos/topic")
  public ResponseEntity<Map<String,Object>> topic(@RequestParam(defaultValue="pedido.aceptado") String tipo,
      @RequestParam(defaultValue="105") Long pedidoId) throws JsonProcessingException {
    producer.publicarEventoTopic(tipo,pedidoId);
    return ResponseEntity.accepted().body(Map.of("publicado",true,"tipo",tipo,"pedidoId",pedidoId));
  }
}
