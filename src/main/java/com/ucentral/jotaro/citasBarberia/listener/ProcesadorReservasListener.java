package com.ucentral.jotaro.citasBarberia.listener;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
import com.ucentral.jotaro.citasBarberia.entity.Reserva;
import com.ucentral.jotaro.citasBarberia.entity.EstadoReserva;
import com.ucentral.jotaro.citasBarberia.repository.ReservaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;


@Service
public class ProcesadorReservasListener {

    private final ReservaRepository reservaRepository;
    private final RabbitTemplate rabbitTemplate; // Para enviar el evento de confirmación

    @Autowired
    public ProcesadorReservasListener(ReservaRepository reservaRepository, RabbitTemplate rabbitTemplate) {
        this.reservaRepository = reservaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PROCESAR_CITAS_NAME)
    @Transactional
    public void procesarSolicitudReserva(Map<String, Object> mensaje) {
        Number idReservaNumber = (Number) mensaje.get("idReserva");
        Long idReserva = idReservaNumber != null ? idReservaNumber.longValue() : null;
        System.out.println("Procesando solicitud de reserva recibida de RabbitMQ para reserva ID: " + idReserva);

        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada para procesar con ID: " + idReserva));

        // Lógica de procesamiento (aquí podría haber validaciones de disponibilidad, etc.)
        // Por ahora, simplemente la confirmamos.
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        // reserva.setFechaUltimaActualizacion(LocalDateTime.now()); // Se actualiza con @PreUpdate
        reservaRepository.save(reserva);
        System.out.println("Reserva ID: " + idReserva + " confirmada y actualizada en BD.");

        // Enviar evento de confirmación al Fanout Exchange
        Map<String, Object> eventoConfirmacion = new HashMap<>();
        eventoConfirmacion.put("idReserva", reserva.getIdReserva());
        eventoConfirmacion.put("idCliente", reserva.getCliente().getIdCliente());
        eventoConfirmacion.put("idServicio", reserva.getServicio().getIdServicio());
        eventoConfirmacion.put("fechaHora", reserva.getFechaHoraInicio().toString());
        eventoConfirmacion.put("estado", reserva.getEstado().toString());
        eventoConfirmacion.put("correoCliente", reserva.getCliente().getCorreoElectronico()); // Para notificaciones
        eventoConfirmacion.put("nombreCliente", reserva.getCliente().getNombre()); // Añadimos nombre del cliente
        eventoConfirmacion.put("nombreServicio", reserva.getServicio().getNombre()); // Añadimos nombre del servicio
        eventoConfirmacion.put("tipo", "RESERVA_CONFIRMADA"); // Añadimos el tipo de evento

        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE_EVENTOS_CITAS_NAME, "", eventoConfirmacion); // Routing key es ignorada por Fanout
        System.out.println("Evento de reserva confirmada enviado al Fanout Exchange para reserva ID: " + idReserva);
    }
}