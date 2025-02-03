package ec.edu.espe.pos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import ec.edu.espe.pos.controller.dto.ValidacionTarjetaDTO;

@FeignClient(name = "validacionTarjeta", url = "http://ec2-3-23-102-137.us-east-2.compute.amazonaws.com")
public interface ValidacionTarjetaClient {

    @PostMapping("/v1/tarjetas/validar")
    ResponseEntity<Void> validarTarjeta(@RequestBody ValidacionTarjetaDTO validacionTarjetaDTO);
} 