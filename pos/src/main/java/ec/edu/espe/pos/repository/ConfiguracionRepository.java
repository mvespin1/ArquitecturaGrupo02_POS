package ec.edu.espe.pos.repository;

import ec.edu.espe.pos.model.Configuracion;
import ec.edu.espe.pos.model.ConfiguracionPK;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, ConfiguracionPK> {
    Optional<Configuracion> findByPk(ConfiguracionPK pk);
}