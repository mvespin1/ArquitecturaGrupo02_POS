package ec.edu.espe.pos.service;

import ec.edu.espe.pos.model.Configuracion;
import ec.edu.espe.pos.model.Transaccion;
import ec.edu.espe.pos.repository.TransaccionRepository;
import ec.edu.espe.pos.client.GatewayTransaccionClient;
import ec.edu.espe.pos.controller.dto.ActualizacionEstadoDTO;
import ec.edu.espe.pos.controller.dto.ComercioDTO;
import ec.edu.espe.pos.controller.dto.FacturacionComercioDTO;
import ec.edu.espe.pos.controller.dto.GatewayTransaccionDTO;
import ec.edu.espe.pos.controller.mapper.TransaccionMapper;
import ec.edu.espe.pos.client.GatewayComercioClient;
import ec.edu.espe.pos.exception.NotFoundException;
import ec.edu.espe.pos.exception.InvalidDataException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Random;

@Service
public class TransaccionService {

    private static final Logger log = LoggerFactory.getLogger(TransaccionService.class);

    public static final String TIPO_PAGO = "PAG";
    public static final String TIPO_REVERSO = "REV";

    public static final String MODALIDAD_SIMPLE = "SIM";
    public static final String MODALIDAD_RECURRENTE = "REC";

    public static final String ESTADO_ENVIADO = "ENV";
    public static final String ESTADO_AUTORIZADO = "AUT";
    public static final String ESTADO_RECHAZADO = "REC";

    public static final String ESTADO_RECIBO_IMPRESO = "IMP";
    public static final String ESTADO_RECIBO_PENDIENTE = "PEN";

    private static final Set<String> MONEDAS_VALIDAS = Set.of("USD", "EUR", "GBP");

    private static final Set<String> MARCAS_VALIDAS = Set.of("MSCD", "VISA", "AMEX", "DINE");

    private final TransaccionRepository transaccionRepository;
    private final GatewayTransaccionClient gatewayClient;
    private final GatewayComercioClient comercioClient;
    private final ConfiguracionService configuracionService;

    public TransaccionService(TransaccionRepository transaccionRepository,
            GatewayTransaccionClient gatewayClient,
            GatewayComercioClient comercioClient,
            ConfiguracionService configuracionService) {
        this.transaccionRepository = transaccionRepository;
        this.gatewayClient = gatewayClient;
        this.comercioClient = comercioClient;
        this.configuracionService = configuracionService;
    }

    @Transactional
    public Transaccion crear(Transaccion transaccion, String datosSensibles,
            Boolean interesDiferido, Integer cuotas) {
        log.info("Iniciando creación de transacción. Datos recibidos: {}", transaccion);

        // Fase 1: Validación y guardado inicial
        Transaccion transaccionInicial = guardarTransaccionInicial(transaccion);
        log.info("Transacción guardada inicialmente: {}", transaccionInicial);
        log.info("Código único de la transacción guardada: {}", transaccionInicial.getCodigoUnicoTransaccion());

        // Fase 2: Procesamiento con el gateway
        return procesarConGateway(transaccionInicial, datosSensibles, interesDiferido, cuotas);
    }

    @Transactional
    public Transaccion guardarTransaccionInicial(Transaccion transaccion) {
        // Validar y transformar marca si es necesario
        if (transaccion.getMarca() == null || transaccion.getMarca().length() > 4
                || !MARCAS_VALIDAS.contains(transaccion.getMarca())) {
            throw new IllegalArgumentException(
                    "Marca inválida. Debe ser una de: " + String.join(", ", MARCAS_VALIDAS));
        }

        // Establecer valores predeterminados
        transaccion.setTipo(TIPO_PAGO);
        transaccion.setModalidad(MODALIDAD_SIMPLE);
        transaccion.setMoneda("USD");
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setEstado(ESTADO_ENVIADO);
        transaccion.setEstadoRecibo(ESTADO_RECIBO_PENDIENTE);

        // Generar código único y detalle
        String codigoUnico = generarCodigoUnico();
        transaccion.setCodigoUnicoTransaccion(codigoUnico);
        transaccion.setDetalle("Transacción POS - " + transaccion.getMarca());

        log.info("Valores establecidos para transacción inicial: marca={}, monto={}",
                transaccion.getMarca(), transaccion.getMonto());

        // Validar campos obligatorios
        validarDatos(transaccion);
        log.info("Validación de campos completada exitosamente");

        // Guardar transacción inicial
        return transaccionRepository.save(transaccion);
    }

    @Transactional
    public Transaccion procesarConGateway(Transaccion transaccion, String datosSensibles,
            Boolean interesDiferido, Integer cuotas) {
        try {
            // Preparar y enviar al gateway
            GatewayTransaccionDTO gatewayDTO = prepararGatewayDTO(transaccion, datosSensibles,
                    interesDiferido, cuotas);
            log.info("Enviando al gateway DTO con datos de tarjeta incluidos");

            ResponseEntity<String> respuesta = gatewayClient.sincronizarTransaccion(gatewayDTO);
            log.info("Respuesta del gateway - Status: {}, Body: {}", 
                    respuesta.getStatusCode(), respuesta.getBody());

            // Actualizar estado basado en el código HTTP y el mensaje
            if (respuesta.getStatusCode().is2xxSuccessful() && 
                respuesta.getBody() != null && 
                respuesta.getBody().contains("aceptada")) {
                transaccion.setEstado(ESTADO_AUTORIZADO);
                log.info("Transacción autorizada");
            } else if (respuesta.getStatusCode().value() == 400 || 
                     (respuesta.getBody() != null && respuesta.getBody().contains("rechazada"))) {
                transaccion.setEstado(ESTADO_RECHAZADO);
                log.info("Transacción rechazada");
            } else if (respuesta.getStatusCode().value() == 202) {
                // Handle the EN_VALIDACION state if necessary
                log.info("Transacción en proceso de validación");
                // You might want to set a different state or log this
            } else {
                log.warn("Estado inesperado recibido: {}", respuesta.getStatusCode());
                transaccion.setEstado(ESTADO_RECHAZADO); // Default to rejected for unexpected states
            }
            
            // Actualizar la transacción con el nuevo estado
            transaccion = transaccionRepository.save(transaccion);
            log.info("Estado de transacción actualizado a: {}", transaccion.getEstado());

            return transaccion;

        } catch (Exception e) {
            log.error("Error al procesar con gateway: {}", e.getMessage());
            transaccion.setEstado(ESTADO_RECHAZADO);
            transaccion = transaccionRepository.save(transaccion);
            log.info("Transacción marcada como rechazada debido a error de comunicación");
            return transaccion;
        }
    }

    private void validarDatos(Transaccion transaccion) {
        if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("El monto debe ser mayor que cero");
        }
        if (transaccion.getMarca() == null || transaccion.getMarca().trim().isEmpty()) {
            throw new InvalidDataException("La marca es obligatoria");
        }
    }

    private GatewayTransaccionDTO prepararGatewayDTO(Transaccion transaccion, String datosSensibles,
            Boolean interesDiferido, Integer cuotas) {
        GatewayTransaccionDTO dto = new GatewayTransaccionDTO();
        Configuracion config = configuracionService.obtenerConfiguracionActual();

        ComercioDTO comercio = new ComercioDTO();
        comercio.setCodigo(config.getCodigoComercio());

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

        return dto;
    }

    @Transactional(readOnly = true)
    public Transaccion obtenerPorCodigoUnico(String codigoUnicoTransaccion) {
        return transaccionRepository.findByCodigoUnicoTransaccion(codigoUnicoTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
    }

    @Transactional
    public void actualizarEstadoTransaccion(ActualizacionEstadoDTO actualizacion) {
        log.info("Actualizando estado de transacción: {}", actualizacion.getCodigoUnicoTransaccion());
        
        Transaccion transaccion = transaccionRepository.findByCodigoUnicoTransaccion(
                actualizacion.getCodigoUnicoTransaccion())
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));

        // Actualizar estado
        transaccion.setEstado(actualizacion.getEstado());
        transaccion.setDetalle(actualizacion.getMensaje());
        
        // Guardar cambios
        transaccionRepository.save(transaccion);
        log.info("Estado de transacción actualizado a: {}", actualizacion.getEstado());
    }

    private String generarCodigoUnico() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("TRX%06d-%d-%02d-%02d-%02d-%02d-%02d-%012d",
                new Random().nextInt(1000000),
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond(),
                1L);
    }
}