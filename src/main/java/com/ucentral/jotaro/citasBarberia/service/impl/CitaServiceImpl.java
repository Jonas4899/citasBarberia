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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    private final RabbitTemplate rabbitTemplate;
    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final ServicioRepository servicioRepository;

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
    @Transactional
    public void solicitarReserva(Long idCliente, Long idServicio, LocalDateTime fechaHora) {
        System.out.println("Solicitando reserva para cliente ID: " + idCliente + ", servicio ID: " + idServicio + " a las " + fechaHora);

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + idCliente));
        Servicio servicio = servicioRepository.findById(idServicio)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + idServicio));

        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setCliente(cliente);
        nuevaReserva.setServicio(servicio);
        nuevaReserva.setFechaHoraInicio(fechaHora);
        nuevaReserva.setEstado(EstadoReserva.PENDIENTE);
        Reserva reservaGuardada = reservaRepository.save(nuevaReserva);
        System.out.println("Reserva guardada inicialmente con ID: " + reservaGuardada.getIdReserva() + " y estado PENDIENTE");

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("tipo", "RESERVA_CREADA");
        mensaje.put("idReserva", reservaGuardada.getIdReserva());
        mensaje.put("idCliente", idCliente);
        mensaje.put("nombreCliente", cliente.getNombre() + " " + cliente.getApellido());
        mensaje.put("idServicio", idServicio);
        mensaje.put("nombreServicio", servicio.getNombre());
        mensaje.put("fechaHora", fechaHora.toString());
        mensaje.put("estado", reservaGuardada.getEstado().toString());
        mensaje.put("correoCliente", cliente.getCorreoElectronico());

        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_CITAS, mensaje);
        System.out.println("Mensaje de solicitud de reserva enviado a RabbitMQ para reserva ID: " + reservaGuardada.getIdReserva());
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE_EVENTOS_CITAS_NAME, "", mensaje);
        System.out.println("Mensaje de estadísticas enviado a RabbitMQ para reserva ID: " + reservaGuardada.getIdReserva());
    }
    
    @Override
    @Transactional
    public void actualizarEstadoReserva(Long idReserva, EstadoReserva nuevoEstado) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setEstado(nuevoEstado);
            reservaRepository.save(reserva);
            
            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("idReserva", reserva.getIdReserva());
            mensaje.put("idCliente", reserva.getCliente().getIdCliente());
            mensaje.put("nombreCliente", reserva.getCliente().getNombre() + " " + reserva.getCliente().getApellido());
            mensaje.put("idServicio", reserva.getServicio().getIdServicio());
            mensaje.put("nombreServicio", reserva.getServicio().getNombre());
            mensaje.put("fechaHora", reserva.getFechaHoraInicio().toString());
            mensaje.put("estado", nuevoEstado.toString());
            
            switch (nuevoEstado) {
                case CONFIRMADA:
                    mensaje.put("tipo", "RESERVA_CONFIRMADA");
                    break;
                case CANCELADA_CLIENTE:
                case CANCELADA_BARBERIA:
                    mensaje.put("tipo", "RESERVA_CANCELADA");
                    break;
                case COMPLETADA:
                    mensaje.put("tipo", "RESERVA_COMPLETADA");
                    break;
                case NO_ASISTIO:
                    mensaje.put("tipo", "RESERVA_NO_ASISTIO");
                    break;
                default:
                    mensaje.put("tipo", "RESERVA_ACTUALIZADA");
            }
            
            rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE_EVENTOS_CITAS_NAME, "", mensaje);
            System.out.println("Mensaje de estadísticas enviado a RabbitMQ para actualización de reserva ID: " + idReserva);
        } else {
            throw new RuntimeException("Reserva no encontrada con ID: " + idReserva);
        }
    }

    @Override
    public ReservaRepository getReservaRepository() {
        return this.reservaRepository;
    }
}