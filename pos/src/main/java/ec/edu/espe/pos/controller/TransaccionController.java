package ec.edu.espe.pos.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ec.edu.espe.pos.service.TransaccionService;
import ec.edu.espe.pos.controller.dto.TransaccionDTO;
import ec.edu.espe.pos.controller.dto.ActualizacionEstadoDTO;
import ec.edu.espe.pos.controller.dto.GatewayTransaccionDTO;
import ec.edu.espe.pos.controller.mapper.TransaccionMapper;
import ec.edu.espe.pos.model.Transaccion;
import ec.edu.espe.pos.exception.NotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//Revisar ConsultarEstado y ActualizarEstado
// Revisar ExceptionHandler

@RestController
@RequestMapping("/v1/transacciones")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "API para la gestión de transacciones en el POS")
public class TransaccionController {
    
    private static final Logger log = LoggerFactory.getLogger(TransaccionController.class);
    private static final String ESTADO_AUTORIZADO = "AUT";
    private static final String ESTADO_RECHAZADO = "REC";

    private final TransaccionService transaccionService;
    private final TransaccionMapper mapper;

    @Operation(summary = "Crear una nueva transacción")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacción creada exitosamente",
                    content = @Content(schema = @Schema(implementation = TransaccionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos de transacción inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<TransaccionDTO> crearTransaccion(
            @Valid @RequestBody TransaccionDTO transaccionDTO) {
        log.info("Creando nueva transacción");
        Transaccion transaccion = mapper.toModel(transaccionDTO);
        Transaccion resultado = transaccionService.crear(
            transaccion, 
            transaccionDTO.getDatosSensibles(),
            transaccionDTO.getInteresDiferido(),
            transaccionDTO.getCuotas()
        );
        return ResponseEntity.ok(mapper.toDTO(resultado));
    }

    @Operation(summary = "Consultar estado de una transacción")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado consultado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    })
    @GetMapping("/{codigoUnicoTransaccion}/estado")
    public ResponseEntity<TransaccionDTO> consultarEstado(
            @Parameter(description = "Código único de la transacción") 
            @PathVariable String codigoUnicoTransaccion) {
        log.info("Consultando estado de transacción: {}", codigoUnicoTransaccion);
        Transaccion transaccion = transaccionService.obtenerPorCodigoUnico(codigoUnicoTransaccion);
        return ResponseEntity.ok(mapper.toDTO(transaccion));
    }

    @Operation(summary = "Actualizar estado de una transacción")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos"),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    })
    @PutMapping("/actualizar-estado")
    public ResponseEntity<Void> actualizarEstado(
            @Valid @RequestBody ActualizacionEstadoDTO actualizacion) {
        log.info("Actualizando estado de transacción: {}", actualizacion.getCodigoUnicoTransaccion());
        transaccionService.actualizarEstadoTransaccion(actualizacion);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<TransaccionDTO> handleNotFoundException(NotFoundException e) {
        TransaccionDTO response = new TransaccionDTO();
        response.setEstado(ESTADO_RECHAZADO);
        response.setDetalle(e.getMessage());
        return ResponseEntity.status(404).body(response);
    }
} 