package ec.edu.espe.pos.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActualizacionEstadoDTO {
    
    @NotNull(message = "El código único de transacción es obligatorio")
    private String codigoUnicoTransaccion;

    @NotNull(message = "El estado es obligatorio")
    @Pattern(regexp = "AUT|REC", message = "El estado debe ser AUT (Autorizado) o REC (Rechazado)")
    private String estado;

    @NotNull(message = "El mensaje es obligatorio") 
    private String mensaje;

    // @NotNull(message = "El detalle es obligatorio")
    private String detalle;
} 