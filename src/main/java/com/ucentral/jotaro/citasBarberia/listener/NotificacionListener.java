package com.ucentral.jotaro.citasBarberia.listener;

import com.ucentral.jotaro.citasBarberia.config.RabbitMQConfig;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class NotificacionListener {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String remitente;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICACIONES_NAME)
    public void recibirEventoReservaParaNotificacion(Map<String, Object> evento) {
        System.out.println("LISTENER NOTIFICACIONES: Evento de reserva recibido: " + evento);
        
        try {
            String correoCliente = (String) evento.get("correoCliente");
            if (correoCliente == null || correoCliente.isEmpty()) {
                System.err.println("Error: No se puede enviar notificación, correo del cliente no disponible en el evento: " + evento);
                return;
            }
            
            String nombreCliente = (String) evento.get("nombreCliente");
            String nombreServicio = (String) evento.get("nombreServicio");
            String fechaHora = (String) evento.get("fechaHora");
            String estado = (String) evento.get("estado");
            
            String asunto = "Confirmación de Reserva - Barbería";
            String cuerpo = "Estimado/a " + nombreCliente + ",\n\n" +
                            "Le confirmamos que su reserva ha sido confirmada.\n\n" +
                            "Detalles de su reserva:\n" +
                            "----------------------------------------\n" +
                            "Servicio: " + nombreServicio + "\n" +
                            "Fecha y Hora: " + fechaHora + "\n" +
                            "Estado: Confirmada\n" +
                            "----------------------------------------\n\n" +
                            "Información importante:\n" +
                            "- Por favor, llegue 5 minutos antes de su cita\n" +
                            "- Si necesita cancelar o reprogramar, hágalo con al menos 24 horas de anticipación\n" +
                            "Si tiene alguna pregunta o necesita asistencia, no dude en contactarnos.\n\n" +
                            "Gracias por elegir nuestros servicios.\n\n" +
                            "Saludos cordiales,\n" +
                            "El equipo de Barbería";
            
            enviarCorreo(correoCliente, asunto, cuerpo);
        } catch (Exception e) {
            System.err.println("Error al procesar la notificación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enviarCorreo(String para, String asunto, String cuerpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(para);
        message.setSubject(asunto);
        message.setText(cuerpo);
        message.setFrom(remitente);
        
        try {
            mailSender.send(message);
            System.out.println("Correo enviado exitosamente a: " + para);
        } catch (Exception e) {
            System.err.println("Error al enviar correo a " + para + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}