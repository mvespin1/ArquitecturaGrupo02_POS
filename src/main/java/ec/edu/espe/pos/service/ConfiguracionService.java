package ec.edu.espe.pos.service;

import ec.edu.espe.pos.model.Configuracion;
import ec.edu.espe.pos.model.ConfiguracionPK;
import ec.edu.espe.pos.repository.ConfiguracionRepository;
import ec.edu.espe.pos.exception.NotFoundException;
import ec.edu.espe.pos.exception.ConfigurationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionService.class);
    private static final String ENTITY_NAME = "Configuracion";
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
    private static final int CODIGO_POS_LENGTH = 10;
    private static final int MODELO_LENGTH = 10;
    private static final String PATRON_ALFANUMERICO = "^[A-Za-z0-9]{%d}$";

    private final ConfiguracionRepository configuracionRepository;

    @Transactional(readOnly = true)
    public List<Configuracion> obtenerTodos() {
        log.info("Obteniendo todas las configuraciones");
        return configuracionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Configuracion obtenerPorId(ConfiguracionPK id) {
        log.info("Buscando configuración con ID: {}", id);
        return configuracionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(id.toString(), ENTITY_NAME));
    }

    @Transactional
    public Configuracion crear(Configuracion configuracion) {
        try {
            log.info("Creando nueva configuración para POS: {}", configuracion.getPk().getCodigo());
            validarConfiguracion(configuracion);
            
            ConfiguracionPK pk = new ConfiguracionPK(
                configuracion.getPk().getCodigo(),
                configuracion.getPk().getModelo()
            );
            configuracion.setPk(pk);
            
            Configuracion configuracionGuardada = configuracionRepository.save(configuracion);
            log.info("Configuración creada exitosamente");
            return configuracionGuardada;
        } catch (Exception e) {
            log.error("Error al crear configuración: {}", e.getMessage());
            throw new ConfigurationException(e.getMessage(), "Creación de configuración");
        }
    }

    @Transactional
    public Configuracion actualizarFechaActivacion(ConfiguracionPK id, LocalDateTime nuevaFechaActivacion) {
        try {
            log.info("Actualizando fecha de activación para configuración: {}", id);
            Configuracion configuracion = obtenerPorId(id);
            validarFechaActivacion(nuevaFechaActivacion);
            configuracion.setFechaActivacion(nuevaFechaActivacion);

            Configuracion configuracionActualizada = configuracionRepository.save(configuracion);
            log.info("Fecha de activación actualizada exitosamente");
            return configuracionActualizada;
        } catch (Exception ex) {
            log.error("Error al actualizar fecha de activación: {}", ex.getMessage());
            throw new ConfigurationException(ex.getMessage(), "Actualización de fecha de activación");
        }
    }

    @Transactional(readOnly = true)
    public Configuracion obtenerConfiguracionActual() {
        log.info("Obteniendo configuración actual del POS");
        List<Configuracion> configuraciones = configuracionRepository.findAll();
        
        if (configuraciones.isEmpty()) {
            log.error("No existe configuración para este POS");
            throw new NotFoundException("configuracion-actual", ENTITY_NAME);
        }
        if (configuraciones.size() > 1) {
            log.error("Se encontraron múltiples configuraciones para el POS");
            throw new ConfigurationException("Múltiples configuraciones", "Validación de unicidad");
        }
        
        return configuraciones.get(0);
    }

    private void validarConfiguracion(Configuracion configuracion) {
        log.debug("Validando configuración");
        validarCodigoPOS(configuracion.getPk().getCodigo());
        validarModelo(configuracion.getPk().getModelo());
        validarDireccionMAC(configuracion.getDireccionMac());
        validarFechaActivacion(configuracion.getFechaActivacion());
        validarCodigoComercio(configuracion.getCodigoComercio());
        validarDuplicados(configuracion);
    }

    private void validarCodigoPOS(String codigoPos) {
        if (codigoPos == null || codigoPos.length() != CODIGO_POS_LENGTH) {
            log.error("Error de validación: código POS con longitud incorrecta");
            throw new ConfigurationException(codigoPos, "Longitud de código POS");
        }
        if (!codigoPos.matches(String.format(PATRON_ALFANUMERICO, CODIGO_POS_LENGTH))) {
            log.error("Error de validación: código POS con formato incorrecto");
            throw new ConfigurationException(codigoPos, "Formato de código POS");
        }
    }

    private void validarModelo(String modelo) {
        if (modelo == null || modelo.length() > MODELO_LENGTH) {
            log.error("Error de validación: modelo con longitud incorrecta");
            throw new ConfigurationException(modelo, "Longitud de modelo");
        }
        if (!modelo.matches(String.format(PATRON_ALFANUMERICO, modelo.length()))) {
            log.error("Error de validación: modelo con formato incorrecto");
            throw new ConfigurationException(modelo, "Formato de modelo");
        }
    }

    private void validarDireccionMAC(String direccionMac) {
        if (direccionMac == null || !MAC_ADDRESS_PATTERN.matcher(direccionMac).matches()) {
            log.error("Error de validación: dirección MAC con formato incorrecto");
            throw new ConfigurationException(direccionMac, "Formato de dirección MAC");
        }
    }

    private void validarFechaActivacion(LocalDateTime fechaActivacion) {
        if (fechaActivacion != null && fechaActivacion.isAfter(LocalDateTime.now())) {
            log.error("Error de validación: fecha de activación posterior a la actual");
            throw new ConfigurationException(fechaActivacion.toString(), "Fecha de activación");
        }
    }

    private void validarCodigoComercio(Integer codigoComercio) {
        if (codigoComercio == null || codigoComercio <= 0) {
            log.error("Error de validación: código de comercio inválido");
            throw new ConfigurationException(
                codigoComercio != null ? codigoComercio.toString() : "null", 
                "Código de comercio");
        }
    }

    private void validarDuplicados(Configuracion configuracion) {
        log.debug("Validando duplicados de configuración");
        configuracionRepository.findAll().stream()
                .filter(config -> !config.getPk().equals(configuracion.getPk()))
                .forEach(config -> {
                    if (config.getDireccionMac().equals(configuracion.getDireccionMac())) {
                        log.error("Error de validación: dirección MAC duplicada");
                        throw new ConfigurationException(
                            configuracion.getDireccionMac(), 
                            "Dirección MAC duplicada");
                    }
                });
    }
}