package ec.edu.espe.pos.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GatewayTransaccionDTO {
    @Valid
    // @NotNull(message = "El comercio es obligatorio")
    private ComercioDTO comercio;

    @Valid
    // @NotNull(message = "La facturación del comercio es obligatoria")
    private FacturacionComercioDTO facturacionComercio;

    // @NotBlank(message = "El tipo de transacción es obligatorio")
    @Size(min = 3, max = 3, message = "El tipo debe tener 3 caracteres")
    @Pattern(regexp = "^(PAG|REV|ANU)$", message = "El tipo debe ser PAG, REV o ANU")
    private String tipo;

    // @NotBlank(message = "La marca es obligatoria")
    @Size(min = 3, max = 4, message = "La marca debe tener entre 2 y 3 caracteres")
    private String marca;

    // @NotBlank(message = "El detalle es obligatorio")
    @Size(max = 100, message = "El detalle no puede exceder los 100 caracteres")
    private String detalle;

    // @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    // @NotBlank(message = "El código único de transacción es obligatorio")
    @Size(min = 10, max = 20, message = "El código único debe tener entre 10 y 20 caracteres")
    private String codigoUnicoTransaccion;

    // @NotNull(message = "La fecha es obligatoria")
    private LocalDateTime fecha;

    // @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(ENV|AUT|REC|REV|ANU)$", message = "Estado inválido")
    private String estado;

    // @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres")
    private String moneda;

    // @NotBlank(message = "El país es obligatorio")
    @Size(min = 3, max = 3, message = "El país debe tener 3 caracteres")
    private String pais;

    // @NotBlank(message = "El número de tarjeta es obligatorio")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe contener solo dígitos")
    private String tarjeta;

    // @NotBlank(message = "El código POS es obligatorio")
    @Size(min = 10, max = 10, message = "El código POS debe tener 10 caracteres")
    private String codigoPos;

    // @NotBlank(message = "El modelo POS es obligatorio")
    @Size(min = 3, max = 3, message = "El modelo POS debe tener 3 caracteres")
    private String modeloPos;

    // @NotNull(message = "El campo interés diferido es obligatorio")
    @Schema(description = "Indica si la transacción tiene interés diferido")    
    private Boolean interesDiferido;

    @Min(value = 0, message = "El número de cuotas no puede ser negativo")
    @Max(value = 12, message = "El número de cuotas no puede exceder 12")
    @Schema(description = "Número de cuotas para el diferido", example = "12")
    private Integer cuotas;

    // @NotBlank(message = "Los datos de la tarjeta son obligatorios")    
    private String datosTarjeta;
}
