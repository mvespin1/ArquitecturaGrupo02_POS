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

// VALIDACIONES
// Map<String, Object> no usar

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/v1/procesamiento-transaccion")
public class ProcesamientoTransaccionController {
    private static final Logger log = LoggerFactory.getLogger(ProcesamientoTransaccionController.class);
    private final TransaccionService transaccionService;
    
    public ProcesamientoTransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping("/procesar")
    public ResponseEntity<Object> procesarPago(@RequestBody Map<String, Object> payload) {
        log.info("Recibiendo petición de pago desde frontend");
        try {
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
            
            // El resto de valores se establecen en el servicio
            Transaccion transaccionProcesada = transaccionService.crear(transaccion, datosSensibles, 
                                                                       interesDiferido, cuotas);
            log.info("Transacción procesada: {}", transaccionProcesada);
            
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", transaccionProcesada.getDetalle());
            return ResponseEntity.ok(response);
            
            //no usar Map<String, si no la parte de programacion en objetos
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error inesperado al procesar pago: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Error al procesar el pago: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}