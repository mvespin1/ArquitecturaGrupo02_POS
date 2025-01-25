package ec.edu.espe.pos.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FacturacionComercioDTO {
    @NotNull(message = "El código de facturación es obligatorio")
    @Positive(message = "El código de facturación debe ser un número positivo")
    private Integer codigo;
}
