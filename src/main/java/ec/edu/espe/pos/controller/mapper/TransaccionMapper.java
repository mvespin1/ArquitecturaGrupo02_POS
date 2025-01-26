package ec.edu.espe.pos.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import ec.edu.espe.pos.controller.dto.GatewayTransaccionDTO;
import ec.edu.espe.pos.model.Transaccion;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransaccionMapper {
    
    GatewayTransaccionDTO toDTO(Transaccion model);
    
    Transaccion toModel(GatewayTransaccionDTO dto);

} 