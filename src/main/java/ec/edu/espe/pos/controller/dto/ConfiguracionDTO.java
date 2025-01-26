package ec.edu.espe.pos.controller.dto;

import java.time.LocalDateTime;
import ec.edu.espe.pos.model.ConfiguracionPK;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfiguracionDTO {

    @Valid
    @NotNull(message = "La clave primaria es obligatoria")
    private ConfiguracionPK pk;

    @NotBlank(message = "La dirección MAC es obligatoria")
    @Size(min = 12, max = 32, message = "La dirección MAC debe tener entre 12 y 32 caracteres")
    private String direccionMac;

    @NotNull(message = "El código del comercio es obligatorio")
    private Integer codigoComercio;

    private LocalDateTime fechaActivacion;
}