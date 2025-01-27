package ec.edu.espe.pos.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GatewayTransaccionDTO {
    @Valid
    private ComercioDTO comercio;

    @Valid
    private FacturacionComercioDTO facturacionComercio;

    @Size(min = 3, max = 3, message = "El tipo debe tener 3 caracteres")
    @Pattern(regexp = "^(PAG|REV|ANU)$", message = "El tipo debe ser PAG, REV o ANU")
    private String tipo;

    @Size(min = 3, max = 4, message = "La marca debe tener entre 2 y 3 caracteres")
    private String marca;

    @Size(max = 100, message = "El detalle no puede exceder los 100 caracteres")
    private String detalle;

    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @Size(min = 10, max = 20, message = "El código único debe tener entre 10 y 20 caracteres")
    private String codigoUnicoTransaccion;

    private LocalDateTime fecha;

    @Pattern(regexp = "^(ENV|AUT|REC|REV|ANU)$", message = "Estado inválido")
    private String estado;

    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres")
    private String moneda;

    @Size(min = 3, max = 3, message = "El país debe tener 3 caracteres")
    private String pais;

    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "\\d{16}", message = "El número de tarjeta debe contener solo dígitos")
    private String tarjeta;

    @Size(min = 10, max = 10, message = "El código POS debe tener 10 caracteres")
    private String codigoPos;

    @Size(min = 3, max = 3, message = "El modelo POS debe tener 3 caracteres")
    private String modeloPos;

    @Schema(description = "Indica si la transacción tiene interés diferido")    
    private Boolean interesDiferido;

    @Min(value = 0, message = "El número de cuotas no puede ser negativo")
    @Max(value = 12, message = "El número de cuotas no puede exceder 12")
    @Schema(description = "Número de cuotas para el diferido", example = "12")
    private Integer cuotas;
  
    private String datosTarjeta;
}
