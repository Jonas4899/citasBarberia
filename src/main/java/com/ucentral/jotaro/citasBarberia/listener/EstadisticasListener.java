package com.ucentral.jotaro.citasBarberia.listener;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EstadisticasListener {

    private static final Logger logger = LoggerFactory.getLogger(EstadisticasListener.class);
    
    // Contador para el total de reservas
    private final AtomicInteger totalReservas = new AtomicInteger(0);
    
    // Conjunto para llevar registro de reservas ya contabilizadas
    private final Set<Long> reservasContabilizadas = ConcurrentHashMap.newKeySet();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTADISTICAS_NAME)
    public void recibirEventoReservaParaEstadisticas(Map<String, Object> evento) {
        logger.info("LISTENER ESTADÍSTICAS: Evento de reserva recibido: {}", evento);
        logger.debug("Claves disponibles en el evento: {}", evento.keySet());
        
        try {
            // Verificar tipo de evento
            String tipoEvento = (String) evento.get("tipo");
            
            if (tipoEvento == null) {
                logger.warn("Evento recibido sin tipo definido: {}", evento);
                return;
            }
            
            switch (tipoEvento) {
                case "RESERVA_CREADA":
                    logger.info("Procesando evento de creación de reserva: {}", evento);
                    procesarEventoReservaCreada(evento);
                    logger.info("Estadísticas actualizadas - Total: {}", totalReservas.get());
                    break;
                case "RESERVA_CONFIRMADA":
                    logger.info("Procesando evento de confirmación de reserva: {}", evento);
                    procesarEventoReservaCreada(evento); // Contabilizamos como una creación si no existe
                    logger.info("Estadísticas actualizadas - Total: {}", totalReservas.get());
                    break;
                case "RESERVA_ACTUALIZADA":
                    procesarEventoActualizada(evento);
                    logger.info("Estadísticas actualizadas después de actualización - Total Reservas: {}", totalReservas.get());
                    break;
                default:
                    logger.info("Tipo de evento no procesado para estadísticas: {}", tipoEvento);
            }
        } catch (Exception e) {
            logger.error("Error al procesar evento para estadísticas: {}", e.getMessage(), e);
            logger.error("Contenido del evento: {}", evento);
        }
    }
    
    private void procesarEventoReservaCreada(Map<String, Object> evento) {
        Long reservaId = obtenerReservaId(evento);
        if (reservaId == null) {
            logger.warn("No se pudo obtener el ID de la reserva del evento: {}", evento);
            return;
        }
        
        logger.debug("ID de reserva obtenido: {}", reservaId);
        
        // Verificar si esta reserva ya ha sido contabilizada
        if (reservasContabilizadas.contains(reservaId)) {
            logger.info("Reserva {} ya contabilizada previamente", reservaId);
            return;
        }
        
        // Marcar como contabilizada
        reservasContabilizadas.add(reservaId);
        logger.debug("Reserva {} añadida a contabilizadas", reservaId);
        
        // Incrementar total de reservas
        int nuevoTotal = totalReservas.incrementAndGet();
        logger.info("Total de reservas incrementado a: {}", nuevoTotal);
    }
    
    private void procesarEventoActualizada(Map<String, Object> evento) {
        Long reservaId = obtenerReservaId(evento);
        if (reservaId == null) {
            logger.warn("No se pudo obtener el ID de la reserva del evento de actualización: {}", evento);
            return;
        }
        
        // Si la reserva no está contabilizada, procesarla como nueva
        if (!reservasContabilizadas.contains(reservaId)) {
            logger.info("Reserva {} no estaba contabilizada, procesando como nueva", reservaId);
            procesarEventoReservaCreada(evento);
        }
    }
    
    private Long obtenerReservaId(Map<String, Object> evento) {
        // Imprimir todas las claves disponibles en el evento para depuración
        logger.debug("Claves disponibles en el evento: {}", evento.keySet());
        
        // Verificar 'idReserva' (nombre usado en la aplicación)
        Object idObj = evento.get("idReserva");
        if (idObj != null) {
            logger.debug("Encontrado ID de reserva como 'idReserva': {}", idObj);
            return convertirALong(idObj);
        }
        
        // Verificar 'reservaId'
        idObj = evento.get("reservaId");
        if (idObj != null) {
            logger.debug("Encontrado ID de reserva como 'reservaId': {}", idObj);
            return convertirALong(idObj);
        }
        
        // Verificar 'id'
        idObj = evento.get("id");
        if (idObj != null) {
            logger.debug("Encontrado ID de reserva como 'id': {}", idObj);
            return convertirALong(idObj);
        }
        
        // Intentar buscar dentro de un objeto "reserva" si existe
        Object reservaObj = evento.get("reserva");
        if (reservaObj instanceof Map) {
            Map<?, ?> reservaMap = (Map<?, ?>) reservaObj;
            logger.debug("Encontrado objeto 'reserva'. Claves: {}", reservaMap.keySet());
            
            idObj = reservaMap.get("id");
            if (idObj != null) {
                logger.debug("Encontrado ID en objeto 'reserva': {}", idObj);
                return convertirALong(idObj);
            }
            
            idObj = reservaMap.get("idReserva");
            if (idObj != null) {
                logger.debug("Encontrado 'idReserva' en objeto 'reserva': {}", idObj);
                return convertirALong(idObj);
            }
        }
        
        logger.warn("No se encontró un ID de reserva válido en ningún campo del evento");
        return null;
    }
    
    private Long convertirALong(Object idObj) {
        if (idObj == null) return null;
        
        logger.debug("Intentando convertir a Long: {} (tipo: {})", idObj, idObj.getClass().getName());
        
        if (idObj instanceof Long) {
            return (Long) idObj;
        } else if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        } else if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        } else if (idObj instanceof String) {
            try {
                return Long.parseLong((String) idObj);
            } catch (NumberFormatException e) {
                logger.warn("No se pudo convertir la cadena a Long: '{}'", idObj);
            }
        }
        
        logger.warn("No se pudo convertir el objeto a Long: {}", idObj);
        return null;
    }
    
    // Método para consultar estadísticas
    public int getTotalReservas() {
        return totalReservas.get();
    }
}