package com.ucentral.jotaro.citasBarberia.listener;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
import com.ucentral.jotaro.citasBarberia.entity.EstadoReserva;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EstadisticasListener {

    private static final Logger logger = LoggerFactory.getLogger(EstadisticasListener.class);
    
    // Contador para el total de reservas
    private final AtomicInteger totalReservas = new AtomicInteger(0);
    
    // Mapas para almacenar estadísticas
    private final Map<String, AtomicInteger> reservasPorServicio = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> reservasPorEstado = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> reservasPorFecha = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTADISTICAS_NAME)
    public void recibirEventoReservaParaEstadisticas(Map<String, Object> evento) {
        logger.info("LISTENER ESTADÍSTICAS: Evento de reserva recibido: {}", evento);
        
        try {
            // Verificar tipo de evento
            String tipoEvento = (String) evento.get("tipo");
            
            if (tipoEvento == null) {
                logger.warn("Evento recibido sin tipo definido");
                return;
            }
            
            switch (tipoEvento) {
                case "RESERVA_CREADA":
                case "RESERVA_CONFIRMADA":
                    procesarEventoReserva(evento);
                    break;
                case "RESERVA_CANCELADA":
                    procesarEventoCancelacion(evento);
                    break;
                case "RESERVA_COMPLETADA":
                    procesarEventoCompletada(evento);
                    break;
                default:
                    logger.info("Tipo de evento no procesado para estadísticas: {}", tipoEvento);
            }
        } catch (Exception e) {
            logger.error("Error al procesar evento para estadísticas: {}", e.getMessage(), e);
        }
    }
    
    private void procesarEventoReserva(Map<String, Object> evento) {
        // Incrementar total de reservas
        totalReservas.incrementAndGet();
        
        // Actualizar reservas por servicio
        String servicio = obtenerNombreServicio(evento);
        if (servicio != null) {
            reservasPorServicio.computeIfAbsent(servicio, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
        // Actualizar reservas por estado
        String estado = (String) evento.get("estado");
        if (estado != null) {
            reservasPorEstado.computeIfAbsent(estado, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
        // Actualizar reservas por fecha
        String fechaStr = obtenerFechaFormateada(evento);
        if (fechaStr != null) {
            reservasPorFecha.computeIfAbsent(fechaStr, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }
    
    private void procesarEventoCancelacion(Map<String, Object> evento) {
        // Actualizar estado de cancelación
        String estado = (String) evento.get("estado");
        if (estado != null) {
            reservasPorEstado.computeIfAbsent(estado, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }
    
    private void procesarEventoCompletada(Map<String, Object> evento) {
        // Actualizar estado de completada
        String estado = (String) evento.get("estado");
        if (estado != null) {
            reservasPorEstado.computeIfAbsent(estado, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }
    
    private String obtenerNombreServicio(Map<String, Object> evento) {
        // Intentar obtener el nombre del servicio desde diferentes campos posibles
        String servicio = (String) evento.get("nombreServicio");
        if (servicio == null) {
            servicio = (String) evento.get("servicio");
        }
        return servicio;
    }
    
    private String obtenerFechaFormateada(Map<String, Object> evento) {
        try {
            String fechaHoraStr = (String) evento.get("fechaHora");
            if (fechaHoraStr != null) {
                LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr);
                return fechaHora.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (Exception e) {
            logger.warn("No se pudo parsear la fecha del evento: {}", e.getMessage());
        }
        return null;
    }
    
    // Métodos para consultar estadísticas
    public int getTotalReservas() {
        return totalReservas.get();
    }
    
    public Map<String, Integer> getReservasPorServicio() {
        Map<String, Integer> resultado = new HashMap<>();
        reservasPorServicio.forEach((key, value) -> resultado.put(key, value.get()));
        return resultado;
    }
    
    public Map<String, Integer> getReservasPorEstado() {
        Map<String, Integer> resultado = new HashMap<>();
        reservasPorEstado.forEach((key, value) -> resultado.put(key, value.get()));
        return resultado;
    }
    
    public Map<String, Integer> getReservasPorFecha() {
        Map<String, Integer> resultado = new HashMap<>();
        reservasPorFecha.forEach((key, value) -> resultado.put(key, value.get()));
        return resultado;
    }
    
    public Map<String, Object> obtenerTodasLasEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalReservas", getTotalReservas());
        estadisticas.put("reservasPorServicio", getReservasPorServicio());
        estadisticas.put("reservasPorEstado", getReservasPorEstado());
        estadisticas.put("reservasPorFecha", getReservasPorFecha());
        return estadisticas;
    }
}