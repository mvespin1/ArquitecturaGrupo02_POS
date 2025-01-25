package ec.edu.espe.pos.repository;

import ec.edu.espe.pos.model.SeguridadGateway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeguridadGatewayRepository extends JpaRepository<SeguridadGateway, Integer> {
    
    boolean existsByClave(String clave);
    
    List<SeguridadGateway> findByEstado(String estado);
    
    Optional<SeguridadGateway> findFirstByEstadoOrderByFechaActualizacionDesc(String estado);
}