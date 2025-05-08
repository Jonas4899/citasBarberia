package com.ucentral.jotaro.citasBarberia.listener;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class EstadisticasListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTADISTICAS_NAME)
    public void recibirEventoReservaParaEstadisticas(Map<String, Object> evento) {
        System.out.println("LISTENER ESTADÍSTICAS: Evento de reserva recibido: " + evento);
        // Lógica para actualizar estadísticas
        // Ej: Incrementar contador de reservas confirmadas, agrupar por servicio, etc.
    }
}