package com.ucentral.jotaro.citasBarberia.service;

import com.ucentral.jotaro.citasBarberia.entity.EstadoReserva;
import com.ucentral.jotaro.citasBarberia.repository.ReservaRepository;
import java.time.LocalDateTime;

public interface CitaService {
    void solicitarReserva(Long idCliente, Long idServicio, LocalDateTime fechaHora);
    
    void actualizarEstadoReserva(Long idReserva, EstadoReserva nuevoEstado);

    ReservaRepository getReservaRepository();
}
