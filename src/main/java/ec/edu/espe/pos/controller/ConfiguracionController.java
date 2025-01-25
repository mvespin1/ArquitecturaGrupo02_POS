package ec.edu.espe.pos.controller;

import ec.edu.espe.pos.model.Configuracion;
import ec.edu.espe.pos.model.ConfiguracionPK;
import ec.edu.espe.pos.service.ConfiguracionService;
import ec.edu.espe.pos.exception.NotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1/pos-configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionController.class);
    private final ConfiguracionService configuracionService;

    // PODRIA SER NO
    @GetMapping
    public ResponseEntity<List<Configuracion>> listarTodos() {
        log.info("Obteniendo todas las configuraciones");
        try {
            return ResponseEntity.ok(configuracionService.obtenerTodos());
        } catch (Exception e) {
            log.error("Error al obtener configuraciones: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{codigo}/{modelo}")
    public ResponseEntity<Configuracion> obtenerPorId(
            @PathVariable String codigo,
            @PathVariable String modelo) {
        log.info("Buscando configuración con código: {} y modelo: {}", codigo, modelo);
        try {
            return ResponseEntity.ok(configuracionService.obtenerPorId(new ConfiguracionPK(codigo, modelo)));
        } catch (NotFoundException e) {
            log.error("Configuración no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al obtener configuración: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Object> crear(@RequestBody Configuracion configuracion) {
        log.info("Creando nueva configuración para POS: {}", configuracion.getPk());
        try {
            Configuracion configuracionCreada = configuracionService.crear(configuracion);
            log.info("Configuración creada exitosamente");
            return ResponseEntity.ok(configuracionCreada);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(crearRespuestaError("Error de validación", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al crear configuración: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(crearRespuestaError("Error interno", "Error al crear la configuración"));
        }
    }

    // PODRIA SER NO
    @PatchMapping("/{codigo}/{modelo}/fecha-activacion")
    public ResponseEntity<Object> actualizarFechaActivacion(
            @PathVariable String codigo,
            @PathVariable String modelo,
            @RequestParam LocalDateTime nuevaFechaActivacion) {
        log.info("Actualizando fecha de activación para configuración: {}/{}", codigo, modelo);
        try {
            Configuracion actualizado = configuracionService.actualizarFechaActivacion(
                    new ConfiguracionPK(codigo, modelo),
                    nuevaFechaActivacion);
            log.info("Fecha de activación actualizada exitosamente");
            return ResponseEntity.ok(actualizado);
        } catch (NotFoundException e) {
            log.error("Configuración no encontrada: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(crearRespuestaError("Error de validación", e.getMessage()));
        } catch (Exception e) {
            log.error("Error al actualizar fecha de activación: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(crearRespuestaError("Error interno", "Error al actualizar la fecha de activación"));
        }
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<Object> recibirConfiguracion(@RequestBody Configuracion configuracion) {
        log.info("Recibiendo configuración para sincronización. PK: {}, MAC: {}",
                configuracion.getPk(), configuracion.getDireccionMac());
        try {
            Configuracion configuracionCreada = configuracionService.crear(configuracion);
            log.info("Configuración sincronizada exitosamente");
            return ResponseEntity.ok(configuracionCreada);
        } catch (Exception e) {
            log.error("Error al sincronizar configuración: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(crearRespuestaError("Error de sincronización", "Error al procesar la configuración"));
        }
    }

    // PODRIA SER NO
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(404).body(crearRespuestaError("No encontrado", e.getMessage()));
    }

    // PODRIA SER NO
    private Map<String, String> crearRespuestaError(String tipo, String mensaje) {
        Map<String, String> response = new HashMap<>();
        response.put("error", tipo);
        response.put("mensaje", mensaje);
        return response;
    }
}