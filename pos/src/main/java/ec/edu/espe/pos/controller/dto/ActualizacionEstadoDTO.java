package ec.edu.espe.pos.controller.dto;

import lombok.Data;

@Data
public class ActualizacionEstadoDTO {
    private String codigoUnicoTransaccion;
    private String estado;
    private String mensaje;
    private String detalle;
} 