package ec.edu.espe.pos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "POS_SEGURIDAD_GATEWAY")
public class SeguridadGateway implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COD_CLAVE_GATEWAY", nullable = false)
    private Integer codigo;

    @NotNull
    @Column(name = "CLAVE", length = 128, nullable = false)
    private String clave;

    @NotNull
    @Column(name = "FECHA_ACTUALIZACION", nullable = false)
    private LocalDateTime fechaActualizacion;

    @NotNull
    @Column(name = "FECHA_ACTIVACION", nullable = false)
    private LocalDate fechaActivacion;

    @NotNull
    @Column(name = "ESTADO", length = 3, nullable = false)
    private String estado;

    public SeguridadGateway(Integer codigo) {
        this.codigo = codigo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SeguridadGateway other = (SeguridadGateway) obj;
        if (codigo == null) {
            if (other.codigo != null)
                return false;
        } else if (!codigo.equals(other.codigo))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PosSeguridadGateway{" +
                "codigo=" + codigo +
                ", clave='" + clave + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", fechaActivacion=" + fechaActivacion +
                ", estado='" + estado + '\'' +
                '}';
    }
}