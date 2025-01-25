package ec.edu.espe.pos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import ec.edu.espe.pos.controller.dto.GatewayTransaccionDTO;

@FeignClient(name = "gateway-transaccion", url = "http://localhost:8082")
public interface GatewayTransaccionClient {

    @PostMapping("/v1/transacciones/sincronizar")
    @ResponseBody
    String sincronizarTransaccion(@RequestBody GatewayTransaccionDTO transaccion);
}
