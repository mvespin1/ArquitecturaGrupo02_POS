package ec.edu.espe.pos.controller;

import ec.edu.espe.pos.model.Configuracion;
import ec.edu.espe.pos.model.ConfiguracionPK;
import ec.edu.espe.pos.service.ConfiguracionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1/pos-configuracion")
@RequiredArgsConstructor
@Tag(name = "Configuración", description = "API para gestionar las configuraciones del POS")
public class ConfiguracionController {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionController.class);
    private final ConfiguracionService configuracionService;

    @Operation(summary = "Obtener todas las configuraciones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de configuraciones encontrada",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Configuracion.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Configuracion>> listarTodos() {
        log.info("Obteniendo todas las configuraciones");
        return ResponseEntity.ok(configuracionService.obtenerTodos());
    }

    @Operation(summary = "Obtener configuración por código y modelo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración encontrada",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Configuracion.class))),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    @GetMapping("/{codigo}/{modelo}")
    public ResponseEntity<Configuracion> obtenerPorId(
            @Parameter(description = "Código de la configuración") @PathVariable String codigo,
            @Parameter(description = "Modelo de la configuración") @PathVariable String modelo) {
        log.info("Buscando configuración con código: {} y modelo: {}", codigo, modelo);
        return ResponseEntity.ok(configuracionService.obtenerPorId(new ConfiguracionPK(codigo, modelo)));
    }

    @Operation(summary = "Crear nueva configuración")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración creada exitosamente",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Configuracion.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Configuración duplicada",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<Configuracion> crear(
            @Parameter(description = "Datos de la configuración a crear")
            @RequestBody Configuracion configuracion) {
        log.info("Creando nueva configuración para POS: {}", configuracion.getPk());
        return ResponseEntity.ok(configuracionService.crear(configuracion));
    }

    @Operation(summary = "Actualizar fecha de activación de una configuración")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fecha de activación actualizada exitosamente",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Configuracion.class))),
        @ApiResponse(responseCode = "400", description = "Fecha inválida",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Configuración no encontrada",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    @PatchMapping("/{codigo}/{modelo}/fecha-activacion")
    public ResponseEntity<Configuracion> actualizarFechaActivacion(
            @Parameter(description = "Código de la configuración") @PathVariable String codigo,
            @Parameter(description = "Modelo de la configuración") @PathVariable String modelo,
            @Parameter(description = "Nueva fecha de activación") @RequestParam LocalDateTime nuevaFechaActivacion) {
        log.info("Actualizando fecha de activación para configuración: {}/{}", codigo, modelo);
        return ResponseEntity.ok(configuracionService.actualizarFechaActivacion(
                new ConfiguracionPK(codigo, modelo),
                nuevaFechaActivacion));
    }

    @Operation(summary = "Sincronizar configuración")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuración sincronizada exitosamente",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Configuracion.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content),
        @ApiResponse(responseCode = "409", description = "Configuración duplicada",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error en la sincronización",
            content = @Content)
    })
    @PostMapping("/sincronizar")
    public ResponseEntity<Configuracion> recibirConfiguracion(
            @Parameter(description = "Configuración a sincronizar")
            @RequestBody Configuracion configuracion) {
        log.info("Recibiendo configuración para sincronización. PK: {}, MAC: {}",
                configuracion.getPk(), configuracion.getDireccionMac());
        return ResponseEntity.ok(configuracionService.crear(configuracion));
    }
}