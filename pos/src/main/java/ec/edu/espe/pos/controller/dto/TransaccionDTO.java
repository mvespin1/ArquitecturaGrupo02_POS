package ec.edu.espe.pos.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la gestión de transacciones en el POS")
public class TransaccionDTO {
    
    @Schema(description = "Código interno de la transacción", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer codigo;

    @NotBlank(message = "El tipo de transacción es requerido")
    @Pattern(regexp = "PAG|REV", message = "El tipo debe ser PAG (Pago) o REV (Reverso)")
    @Schema(description = "Tipo de transacción", example = "PAG")
    private String tipo;

    @NotBlank(message = "La marca de la tarjeta es requerida")
    @Pattern(regexp = "MSCD|VISA|AMEX|DINE", message = "La marca debe ser MSCD, VISA, AMEX o DINE")
    @Schema(description = "Marca de la tarjeta", example = "VISA")
    private String marca;

    @NotBlank(message = "La modalidad es requerida")
    @Pattern(regexp = "SIM|REC", message = "La modalidad debe ser SIM (Simple) o REC (Recurrente)")
    @Schema(description = "Modalidad de la transacción", example = "SIM")
    private String modalidad;

    @NotBlank(message = "El detalle es requerido")
    @Size(max = 255, message = "El detalle no puede exceder los 255 caracteres")
    @Schema(description = "Detalle de la transacción")
    private String detalle;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El monto no puede exceder 999999.99")
    @Digits(integer = 6, fraction = 2, message = "El monto debe tener máximo 6 dígitos enteros y 2 decimales")
    @Schema(description = "Monto de la transacción", example = "100.00")
    private BigDecimal monto;

    @Schema(description = "Código único de la transacción", accessMode = Schema.AccessMode.READ_ONLY)
    private String codigoUnicoTransaccion;

    @Schema(description = "Fecha de la transacción", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fecha;

    @Schema(description = "Estado de la transacción", accessMode = Schema.AccessMode.READ_ONLY)
    private String estado;

    @Schema(description = "Estado del recibo", accessMode = Schema.AccessMode.READ_ONLY)
    private String estadoRecibo;

    @NotBlank(message = "La moneda es requerida")
    @Pattern(regexp = "USD", message = "La moneda debe ser USD")
    @Schema(description = "Moneda de la transacción", example = "USD")
    private String moneda;

    @Schema(description = "Datos sensibles de la tarjeta (encriptados)")
    private String datosSensibles;

    @Schema(description = "Indica si la transacción tiene interés diferido")
    private Boolean interesDiferido;

    @Min(value = 0, message = "El número de cuotas no puede ser negativo")
    @Max(value = 48, message = "El número de cuotas no puede exceder 48")
    @Schema(description = "Número de cuotas para el diferido", example = "12")
    private Integer cuotas;
} 