package com.ucentral.jotaro.citasBarberia.service.impl;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
import com.ucentral.jotaro.citasBarberia.entity.Cliente;
import com.ucentral.jotaro.citasBarberia.entity.Reserva;
import com.ucentral.jotaro.citasBarberia.entity.Servicio;
import com.ucentral.jotaro.citasBarberia.entity.EstadoReserva;
import com.ucentral.jotaro.citasBarberia.repository.ClienteRepository;
import com.ucentral.jotaro.citasBarberia.repository.ReservaRepository;
import com.ucentral.jotaro.citasBarberia.repository.ServicioRepository;
import com.ucentral.jotaro.citasBarberia.service.CitaService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante

import java.time.LocalDateTime;
import java.util.HashMap; // O un DTO específico para el mensaje
import java.util.Map;

@Service
public class CitaServiceImpl implements CitaService {

    private final RabbitTemplate rabbitTemplate;
    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository; // Para obtener el cliente
    private final ServicioRepository servicioRepository; // Para obtener el servicio

    @Autowired
    public CitaServiceImpl(RabbitTemplate rabbitTemplate,
                           ReservaRepository reservaRepository,
                           ClienteRepository clienteRepository,
                           ServicioRepository servicioRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional // Para asegurar que la reserva se guarde y el mensaje se envíe, o ninguno
    public void solicitarReserva(Long idCliente, Long idServicio, LocalDateTime fechaHora) {
        System.out.println("Solicitando reserva para cliente ID: " + idCliente + ", servicio ID: " + idServicio + " a las " + fechaHora);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + idCliente));
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + idServicio));

        // 1. Opcional: Crear y guardar la reserva con estado PENDIENTE
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setCliente(cliente);
        nuevaReserva.setServicio(servicio);
        nuevaReserva.setFechaHoraInicio(fechaHora);
        nuevaReserva.setEstado(EstadoReserva.PENDIENTE); // Se actualiza en onCreate()
        // nuevaReserva.setFechaCreacion(LocalDateTime.now()); // Se actualiza con @PrePersist
        Reserva reservaGuardada = reservaRepository.save(nuevaReserva);
        System.out.println("Reserva guardada inicialmente con ID: " + reservaGuardada.getIdReserva() + " y estado PENDIENTE");


        // 2. Preparar y enviar mensaje a RabbitMQ
        // Puedes enviar el ID de la reserva, o todos los datos necesarios para procesarla.
        // Enviar el ID es más simple si el consumidor puede luego leer de la BD.
        // Enviar todos los datos desacopla más al consumidor de la BD en esta etapa.
        // Por ahora, enviemos los IDs y la fecha/hora.
        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("idReserva", reservaGuardada.getIdReserva()); // Importante para luego actualizarla
        mensaje.put("idCliente", idCliente);
        mensaje.put("idServicio", idServicio);
        mensaje.put("fechaHora", fechaHora.toString()); // Convertir a String para serialización simple

        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_CITAS, mensaje);
        System.out.println("Mensaje de solicitud de reserva enviado a RabbitMQ para reserva ID: " + reservaGuardada.getIdReserva());
    }
}