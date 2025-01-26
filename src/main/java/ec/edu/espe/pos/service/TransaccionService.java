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

        if (transaccion.getMarca() == null || transaccion.getMarca().length() > 4
                || !MARCAS_VALIDAS.contains(transaccion.getMarca())) {
            log.error("Marca inválida: {}", transaccion.getMarca());
            throw new InvalidDataException(
                    "Marca inválida. Debe ser una de: " + String.join(", ", MARCAS_VALIDAS));
        }

        transaccion.setTipo(TIPO_PAGO);
        transaccion.setModalidad(MODALIDAD_SIMPLE);
        transaccion.setMoneda("USD");
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setEstado(ESTADO_ENVIADO);
        transaccion.setEstadoRecibo(ESTADO_RECIBO_PENDIENTE);
        String codigoUnico = generarCodigoUnico();
        transaccion.setCodigoUnicoTransaccion(codigoUnico);
        transaccion.setDetalle("Transacción POS - " + transaccion.getMarca());

        log.info("Valores establecidos para transacción: marca={}, monto={}, codigoUnico={}",
                transaccion.getMarca(), transaccion.getMonto(), transaccion.getCodigoUnicoTransaccion());

        validarCamposObligatorios(transaccion);
        log.info("Validación de campos completada exitosamente");

        if (interesDiferido == null) {
            interesDiferido = false;
        }
        if (cuotas == null) {
            cuotas = 0;
        }

        log.info("Guardando transacción en la base de datos: {}", transaccion);

        // Guardar la transacción
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);
        log.info("Transacción guardada localmente con ID: {}", transaccionGuardada.getCodigo());

        try {
            GatewayTransaccionDTO gatewayDTO = convertirAGatewayDTO(transaccionGuardada, datosSensibles,
                    interesDiferido, cuotas);
            log.info("DTO preparado para enviar al gateway: {}", gatewayDTO);

            String respuesta = gatewayClient.sincronizarTransaccion(gatewayDTO);
            log.info("Respuesta del gateway: {}", respuesta);

            transaccionGuardada.setDetalle(respuesta);
            transaccionGuardada = transaccionRepository.save(transaccionGuardada);

            log.info("Transacción actualizada con respuesta del gateway: {}", transaccionGuardada);
            return transaccionGuardada;
        } catch (Exception e) {
            log.error("Error al sincronizar con el gateway: {}", e.getMessage());
            throw new InvalidDataException("Error al procesar la transacción: " + e.getMessage());
        }
    }

    private void validarCamposObligatorios(Transaccion transaccion) {
        if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("El monto debe ser mayor que cero");
        }
        if (transaccion.getMarca() == null || transaccion.getMarca().trim().isEmpty()) {
            throw new InvalidDataException("La marca es obligatoria");
        }
        if (transaccion.getDetalle() == null || transaccion.getDetalle().trim().isEmpty()) {
            throw new InvalidDataException("El detalle es obligatorio");
        }
        if (!MONEDAS_VALIDAS.contains(transaccion.getMoneda())) {
            throw new InvalidDataException("Moneda no válida");
        }
    }

    private GatewayTransaccionDTO convertirAGatewayDTO(Transaccion transaccion, String datosSensibles,
            Boolean interesDiferido, Integer cuotas) {
            
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

            log.info("DTO creado para el gateway: {}", dto);

        } catch (Exception e) {
            log.error("Error al obtener datos del comercio: {}", e.getMessage());
            throw new InvalidDataException("Error al preparar datos para el gateway: " + e.getMessage());
        }

        return dto;
    }

    public Transaccion obtenerPorCodigoUnico(String codigoUnico) {
        return transaccionRepository.findByCodigoUnicoTransaccion(codigoUnico)
                .orElseThrow(() -> new NotFoundException(codigoUnico, "Transaccion"));
    }

    public void actualizarEstadoTransaccion(ActualizacionEstadoDTO actualizacion) {
        Transaccion transaccion = obtenerPorCodigoUnico(actualizacion.getCodigoUnicoTransaccion());
        transaccion.setEstado(actualizacion.getEstado());
        transaccionRepository.save(transaccion);
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
}