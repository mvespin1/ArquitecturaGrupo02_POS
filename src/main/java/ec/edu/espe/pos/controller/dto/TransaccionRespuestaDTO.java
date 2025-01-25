package ec.edu.espe.pos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "DTO para la respuesta de procesamiento de transacciones")
public class TransaccionRespuestaDTO {
    
    @Schema(description = "Mensaje de respuesta de la transacción")
    private String mensaje;
    
    @Schema(description = "Código único de la transacción procesada")
    private String codigoUnicoTransaccion;
    
    @Schema(description = "Estado de la transacción")
    private String estado;
}