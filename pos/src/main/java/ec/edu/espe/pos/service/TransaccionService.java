package ec.edu.espe.pos.service;

import ec.edu.espe.pos.model.Configuracion;
import ec.edu.espe.pos.model.Transaccion;
import ec.edu.espe.pos.repository.TransaccionRepository;
import ec.edu.espe.pos.client.GatewayTransaccionClient;
import ec.edu.espe.pos.controller.dto.ActualizacionEstadoDTO;
import ec.edu.espe.pos.controller.dto.ComercioDTO;
import ec.edu.espe.pos.controller.dto.FacturacionComercioDTO;
import ec.edu.espe.pos.controller.dto.GatewayTransaccionDTO;
import ec.edu.espe.pos.client.GatewayComercioClient;
import ec.edu.espe.pos.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.Random;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private static final Logger log = LoggerFactory.getLogger(TransaccionService.class);
    private static final String ENTITY_NAME = "Transaccion";

    // Constantes para tipos de transacción
    public static final String TIPO_PAGO = "PAG";
    public static final String TIPO_REVERSO = "REV";

    // Constantes para modalidades
    public static final String MODALIDAD_SIMPLE = "SIM";
    public static final String MODALIDAD_RECURRENTE = "REC";

    // Constantes para estados
    public static final String ESTADO_ENVIADO = "ENV";
    public static final String ESTADO_AUTORIZADO = "AUT";
    public static final String ESTADO_RECHAZADO = "REC";

    // Constantes para estados de recibo
    public static final String ESTADO_RECIBO_IMPRESO = "IMP";
    public static final String ESTADO_RECIBO_PENDIENTE = "PEN";

    // Set de códigos de moneda válidos (ISO 4217)
    private static final Set<String> MONEDAS_VALIDAS = Set.of("USD", "EUR", "GBP");
    private static final Set<String> MARCAS_VALIDAS = Set.of("MSCD", "VISA", "AMEX", "DINE");

    private final TransaccionRepository transaccionRepository;
    private final GatewayTransaccionClient gatewayClient;
    private final GatewayComercioClient comercioClient;
    private final ConfiguracionService configuracionService;

    @Transactional
    public Transaccion crear(Transaccion transaccion, String datosSensibles, Boolean interesDiferido, Integer cuotas) {
        log.info("Iniciando creación de transacción. Datos recibidos: {}", transaccion);
        Transaccion transaccionInicial = guardarTransaccionInicial(transaccion);
        log.info("Transacción guardada inicialmente: {}", transaccionInicial);
        return procesarConGateway(transaccionInicial, datosSensibles, interesDiferido, cuotas);
    }

    @Transactional(readOnly = true)
    public Transaccion obtenerPorCodigoUnico(String codigoUnicoTransaccion) {
        return transaccionRepository.findByCodigoUnicoTransaccion(codigoUnicoTransaccion)
                .orElseThrow(() -> new NotFoundException(codigoUnicoTransaccion, ENTITY_NAME));
    }

    @Transactional
    public void actualizarEstadoTransaccion(ActualizacionEstadoDTO actualizacion) {
        log.info("Actualizando estado de transacción: {}", actualizacion.getCodigoUnicoTransaccion());
        try {
            Transaccion transaccion = obtenerPorCodigoUnico(actualizacion.getCodigoUnicoTransaccion());
            
            // Validar transición de estados
            if (validarTransicionEstado(transaccion.getEstado(), actualizacion.getEstado())) {
                transaccion.setEstado(actualizacion.getEstado());
                transaccion.setDetalle(limitarLongitudMensaje(actualizacion.getMensaje()));
                transaccionRepository.save(transaccion);
                log.info("Estado de transacción actualizado a: {}", actualizacion.getEstado());
            } else {
                log.error("Transición de estado no válida: {} -> {}", 
                    transaccion.getEstado(), actualizacion.getEstado());
            }
        } catch (Exception e) {
            log.error("Error al actualizar estado: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Transaccion guardarTransaccionInicial(Transaccion transaccion) {
        // Establecer valores predeterminados obligatorios
        transaccion.setEstado(ESTADO_ENVIADO);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setCodigoUnicoTransaccion(generarCodigoUnico());
        transaccion.setTipo(TIPO_PAGO);
        transaccion.setModalidad(MODALIDAD_SIMPLE);
        transaccion.setMoneda("USD");
        transaccion.setDetalle("Transacción POS - " + transaccion.getMarca());
        transaccion.setEstadoRecibo(ESTADO_RECIBO_PENDIENTE);

        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public Transaccion procesarConGateway(Transaccion transaccion, String datosSensibles, Boolean interesDiferido, Integer cuotas) {
        try {
            GatewayTransaccionDTO gatewayDTO = convertirAGatewayDTO(transaccion, datosSensibles, interesDiferido, cuotas);
            log.info("Enviando al gateway DTO con datos de tarjeta incluidos");

            ResponseEntity<GatewayTransaccionDTO> respuesta = gatewayClient.sincronizarTransaccion(gatewayDTO);
            log.info("Respuesta del gateway - Status: {}, Body: {}", respuesta.getStatusCode(), respuesta.getBody());

            if (respuesta.getStatusCode().is2xxSuccessful()) {
                transaccion.setEstado(ESTADO_AUTORIZADO);
            } else {
                transaccion.setEstado(ESTADO_RECHAZADO);
            }
            return transaccionRepository.save(transaccion);

        } catch (Exception e) {
            log.error("Error al procesar con gateway: {}", e.getMessage());
            transaccion.setEstado(ESTADO_RECHAZADO);
            return transaccionRepository.save(transaccion);
        }
    }

    private void validarMarca(String marca) {
        if (marca == null || marca.length() > 4 || !MARCAS_VALIDAS.contains(marca)) {
            throw new NotFoundException(marca, "Marca válida. Debe ser una de: " + String.join(", ", MARCAS_VALIDAS));
        }
    }

    private void establecerValoresPredeterminados(Transaccion transaccion) {
        transaccion.setTipo(TIPO_PAGO);
        transaccion.setModalidad(MODALIDAD_SIMPLE);
        transaccion.setMoneda("USD");
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setEstado(ESTADO_ENVIADO);
        transaccion.setEstadoRecibo(ESTADO_RECIBO_PENDIENTE);
        transaccion.setCodigoUnicoTransaccion("TRX" + System.currentTimeMillis());
        transaccion.setDetalle("Transacción POS - " + transaccion.getMarca());
    }

    private void validarCamposObligatorios(Transaccion transaccion) {
        if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NotFoundException(transaccion.getMonto() != null ? transaccion.getMonto().toString() : "null", 
                "Monto. Debe ser mayor que cero");
        }
        if (transaccion.getMarca() == null || transaccion.getMarca().trim().isEmpty()) {
            throw new NotFoundException("null", "Marca. Es un campo obligatorio");
        }
        if (transaccion.getDetalle() == null || transaccion.getDetalle().trim().isEmpty()) {
            throw new NotFoundException("null", "Detalle. Es un campo obligatorio");
        }
        if (!MONEDAS_VALIDAS.contains(transaccion.getMoneda())) {
            throw new NotFoundException(transaccion.getMoneda(), 
                "Moneda válida. Debe ser una de: " + String.join(", ", MONEDAS_VALIDAS));
        }
    }

    private GatewayTransaccionDTO convertirAGatewayDTO(Transaccion transaccion, String datosSensibles, Boolean interesDiferido, Integer cuotas) {
        GatewayTransaccionDTO dto = new GatewayTransaccionDTO();
        
        try {
            log.info("Obteniendo configuración actual del POS");
            Configuracion config = configuracionService.obtenerConfiguracionActual();
            
            ComercioDTO comercio = new ComercioDTO();
            comercio.setCodigo(config.getCodigoComercio());
            
            log.info("Consultando facturación para el comercio: {}", comercio.getCodigo());
            FacturacionComercioDTO facturacion = comercioClient.obtenerFacturacionPorComercio(comercio.getCodigo());
            
            dto.setComercio(comercio);
            dto.setFacturacionComercio(facturacion);
            dto.setTipo(transaccion.getModalidad());
            dto.setMarca(transaccion.getMarca());
            dto.setDetalle(transaccion.getDetalle());
            dto.setMonto(transaccion.getMonto());
            dto.setCodigoUnicoTransaccion(transaccion.getCodigoUnicoTransaccion());
            dto.setFecha(transaccion.getFecha());
            dto.setEstado(transaccion.getEstado());
            dto.setMoneda(transaccion.getMoneda());
            dto.setPais("EC");
            dto.setCodigoPos(config.getPk().getCodigo());
            dto.setModeloPos(config.getPk().getModelo());
            dto.setTarjeta(datosSensibles);
            dto.setInteresDiferido(interesDiferido);
            dto.setCuotas(cuotas);

            log.info("DTO preparado para enviar al gateway. Comercio código: {}, POS código: {}, modelo: {}",
                    comercio.getCodigo(), config.getPk().getCodigo(), config.getPk().getModelo());

        } catch (Exception e) {
            log.error("Error al obtener datos del comercio: {}", e.getMessage());
            throw new NotFoundException("Error al preparar datos", "Gateway");
        }

        return dto;
    }

    private String generarCodigoUnico() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("TRX%06d-%d-%02d-%02d-%02d-%02d-%02d-%012d",
            new Random().nextInt(1000000), // simulando un ID secuencial
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            now.getHour(),
            now.getMinute(),
            now.getSecond(),
            1L); // ID de facturación, ajustar según necesidad
    }

    private boolean validarTransicionEstado(String estadoActual, String nuevoEstado) {
        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        switch (estadoActual) {
            case ESTADO_ENVIADO:
                return ESTADO_AUTORIZADO.equals(nuevoEstado) || 
                       ESTADO_RECHAZADO.equals(nuevoEstado);
            case ESTADO_AUTORIZADO:
                return false; // No se permite cambio desde autorizado
            case ESTADO_RECHAZADO:
                return false; // No se permite cambio desde rechazado
            default:
                return false;
        }
    }

    private String limitarLongitudMensaje(String mensaje) {
        final int MAX_LENGTH = 200;
        if (mensaje == null) {
            return "";
        }
        return mensaje.length() > MAX_LENGTH ? mensaje.substring(0, MAX_LENGTH) : mensaje;
    }
}