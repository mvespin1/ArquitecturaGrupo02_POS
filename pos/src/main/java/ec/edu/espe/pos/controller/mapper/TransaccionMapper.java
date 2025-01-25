package ec.edu.espe.pos.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import ec.edu.espe.pos.controller.dto.TransaccionDTO;
import ec.edu.espe.pos.model.Transaccion;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TransaccionMapper {
    
    TransaccionDTO toDTO(Transaccion model);
    
    Transaccion toModel(TransaccionDTO dto);
} 