package com.ucentral.jotaro.citasBarberia.service;

import com.ucentral.jotaro.citasBarberia.entity.EstadoReserva;
import com.ucentral.jotaro.citasBarberia.repository.ReservaRepository;
import java.time.LocalDateTime;

public interface CitaService {
    /**
     * Método para solicitar una reserva
     * @param idCliente ID del cliente que solicita la reserva
     * @param idServicio ID del servicio solicitado
     * @param fechaHora Fecha y hora de la reserva
     */
    void solicitarReserva(Long idCliente, Long idServicio, LocalDateTime fechaHora);
    
    /**
     * Método para actualizar el estado de una reserva
     * @param idReserva ID de la reserva a actualizar
     * @param nuevoEstado Nuevo estado de la reserva
     */
    void actualizarEstadoReserva(Long idReserva, EstadoReserva nuevoEstado);

    /**
     * Obtener el repositorio de reservas
     * @return El repositorio de reservas
     */
    ReservaRepository getReservaRepository();
}
