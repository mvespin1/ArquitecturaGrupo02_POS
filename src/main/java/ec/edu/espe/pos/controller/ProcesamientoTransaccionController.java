package ec.edu.espe.pos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Procesamiento de Transacciones", description = "API para procesar transacciones de pago en el POS")
public class ProcesamientoTransaccionController {
    private static final Logger log = LoggerFactory.getLogger(ProcesamientoTransaccionController.class);
    private final TransaccionService transaccionService;
    
    public ProcesamientoTransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @Operation(summary = "Procesar una nueva transacción de pago",
               description = "Procesa una transacción de pago con los datos de la tarjeta y opciones de diferido")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción procesada exitosamente",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Transaccion.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json",
            schema = @Schema(type = "object", example = "{\"mensaje\": \"Error de validación: Monto inválido\"}"))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json",
            schema = @Schema(type = "object", example = "{\"mensaje\": \"Error al procesar el pago\"}")))
    })
    @PostMapping("/procesar")
    public ResponseEntity<Object> procesarPago(
            @Parameter(description = "Datos de la transacción", required = true,
                      schema = @Schema(type = "object", example = """
                      {
                        "monto": "100.50",
                        "marca": "VISA",
                        "datosTarjeta": "datos_tarjeta",
                        "interesDiferido": true,
                        "cuotas": 3
                      }
                      """))
            @RequestBody Map<String, Object> payload) {
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