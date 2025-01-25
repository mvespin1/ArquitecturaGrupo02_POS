package ec.edu.espe.pos.repository;

import ec.edu.espe.pos.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {
    Optional<Transaccion> findByCodigoUnicoTransaccion(String codigoUnicoTransaccion);
    List<Transaccion> findByEstado(String estado);
    List<Transaccion> findByTipoAndEstado(String tipo, String estado);
    Boolean existsByCodigoUnicoTransaccion(String codigoUnicoTransaccion);
}