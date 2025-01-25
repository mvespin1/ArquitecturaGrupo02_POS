package ec.edu.espe.pos.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

// SI SE UTILIZA

@Data
@NoArgsConstructor
public class ComercioDTO {
    @NotNull(message = "El código del comercio es obligatorio")
    @Positive(message = "El código del comercio debe ser un número positivo")
    private Integer codigo;
}