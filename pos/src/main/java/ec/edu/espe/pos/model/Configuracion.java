package ec.edu.espe.pos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// ACTUALIZACION A UTILIZAR LOMBOK

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "POS_CONFIGURACION")
public class Configuracion implements Serializable {

    @EmbeddedId
    private ConfiguracionPK pk;

    @NotNull
    @Column(name = "DIRECCION_MAC", length = 32, nullable = false)
    private String direccionMac;

    @NotNull
    @Column(name = "CODIGO_COMERCIO", nullable = false)
    private Integer codigoComercio;

    @NotNull
    @Column(name = "FECHA_ACTIVACION", nullable = false)
    private LocalDateTime fechaActivacion;

    @Transient
    private String codigoComercioPOS;

    public Configuracion(ConfiguracionPK pk) {
        this.pk = pk;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pk == null) ? 0 : pk.hashCode());
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
        Configuracion other = (Configuracion) obj;
        if (pk == null) {
            if (other.pk != null)
                return false;
        } else if (!pk.equals(other.pk))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "POSConfiguracion [pk=" + pk + ", direccionMac=" + direccionMac + ", codigoComercio=" + codigoComercio
                + ", fechaActivacion=" + fechaActivacion + "]";
    }

}