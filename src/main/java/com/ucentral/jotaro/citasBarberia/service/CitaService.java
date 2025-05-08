package com.ucentral.jotaro.citasBarberia.service;

public interface CitaService {
    // void solicitarReserva(SolicitudReservaDto solicitud);
    void solicitarReserva(Long idCliente, Long idServicio, java.time.LocalDateTime fechaHora); // Ejemplo simplificado
}
