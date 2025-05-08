package com.ucentral.jotaro.citasBarberia.listener;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
// Importar Spring Mail si vas a enviar correos:
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificacionListener {

    // @Autowired
    // private JavaMailSender mailSender; // Descomentar si configuras spring-boot-starter-mail

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES_NAME)
    public void recibirEventoReservaParaNotificacion(Map<String, Object> evento) {
        System.out.println("LISTENER NOTIFICACIONES: Evento de reserva recibido: " + evento);
        // String correoCliente = (String) evento.get("correoCliente");
        // String asunto = "Confirmación de Reserva - Barbería XYZ";
        // String cuerpo = "Su reserva para el servicio " + evento.get("idServicio") +
        //                 " el día " + evento.get("fechaHora") + " ha sido " + evento.get("estado") + ".";
        // enviarCorreo(correoCliente, asunto, cuerpo);
        // Lógica para enviar notificación (email, SMS, etc.)
    }

    /*
    private void enviarCorreo(String para, String asunto, String cuerpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(para);
        message.setSubject(asunto);
        message.setText(cuerpo);
        // message.setFrom("noreply@barberia.com"); // Configurar en application.properties
        // mailSender.send(message);
        System.out.println("Simulando envío de correo a: " + para + " | Asunto: " + asunto);
    }
    */
}