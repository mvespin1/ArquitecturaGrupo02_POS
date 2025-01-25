package ec.edu.espe.pos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ec.edu.espe.pos.service.TransaccionService;
import ec.edu.espe.pos.model.Transaccion;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

import lombok.RequiredArgsConstructor;

// CAMBIAR NOMBRE DE LA CLASE A PROCESAMIENTO TRANSACCION CONTROLLER  
// API TAMBIEN
// VALIDACIONES
// guardarTransaccionInicial A guardarTransaccionPos
// Map<String, Object> no usar

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/v1/pagos")
@RequiredArgsConstructor
public class PagoController {
    private static final Logger log = LoggerFactory.getLogger(PagoController.class);
    private final TransaccionService transaccionService;

    @PostMapping("/procesar")
    public ResponseEntity<Object> procesarPago(@RequestBody Map<String, Object> payload) {
        log.info("Recibiendo petición de pago desde frontend");
        try {
            validarPayload(payload);
            
            // Crear objeto transacción con datos básicos
            Transaccion transaccion = new Transaccion();
            transaccion.setMonto(new BigDecimal(payload.get("monto").toString()));
            transaccion.setMarca(payload.get("marca").toString());
            
            // Obtener datos sensibles encriptados
            String datosSensibles = payload.get("datosTarjeta").toString();
            
            // Obtener datos de diferido
            Boolean interesDiferido = payload.get("interesDiferido") != null ? 
                                    (Boolean) payload.get("interesDiferido") : false;
            Integer cuotas = interesDiferido && payload.get("cuotas") != null ? 
                           Integer.valueOf(payload.get("cuotas").toString()) : null;
            
            log.info("Datos de diferido - Interés: {}, Cuotas: {}", interesDiferido, cuotas);
            
            // Primero guardamos la transacción inicial
            Transaccion transaccionInicial = transaccionService.guardarTransaccionInicial(transaccion);
            log.info("Transacción guardada inicialmente: {}", transaccionInicial);

            // Devolvemos respuesta inmediata
            Map<String, Object> responseInicial = crearRespuestaExitosa(transaccionInicial);

            // Procesamos en segundo plano (utilizar el anterior)
            procesarTransaccionEnSegundoPlano(transaccionInicial, datosSensibles, interesDiferido, cuotas);

            return ResponseEntity.status(201).body(responseInicial);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(crearRespuestaError("Error de validación", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al procesar pago: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(crearRespuestaError("Error interno", "Error al procesar el pago"));
        }
    }

    private void validarPayload(Map<String, Object> payload) {
        if (payload.get("monto") == null) {
            throw new IllegalArgumentException("El monto es requerido");
        }
        if (payload.get("marca") == null) {
            throw new IllegalArgumentException("La marca es requerida");
        }
        if (payload.get("datosTarjeta") == null) {
            throw new IllegalArgumentException("Los datos de la tarjeta son requeridos");
        }
    }

    private Map<String, Object> crearRespuestaExitosa(Transaccion transaccion) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Transacción registrada, procesando pago...");
        response.put("estado", "pending");
        response.put("transaccion", transaccion);
        return response;
    }

    private Map<String, String> crearRespuestaError(String tipo, String mensaje) {
        Map<String, String> response = new HashMap<>();
        response.put("error", tipo);
        response.put("mensaje", mensaje);
        return response;
    }

    private void procesarTransaccionEnSegundoPlano(
            Transaccion transaccion, 
            String datosSensibles, 
            Boolean interesDiferido, 
            Integer cuotas) {
        new Thread(() -> {
            try {
                transaccionService.procesarConGateway(transaccion, datosSensibles, 
                                                    interesDiferido, cuotas);
            } catch (Exception e) {
                log.error("Error en procesamiento asíncrono: {}", e.getMessage());
            }
        }).start();
    }
}
