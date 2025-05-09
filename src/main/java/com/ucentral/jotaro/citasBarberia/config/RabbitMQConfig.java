package com.ucentral.jotaro.citasBarberia.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String DIRECT_EXCHANGE_NAME = "citas.direct.exchange";
    public static final String QUEUE_PROCESAR_CITAS_NAME = "cola_procesar_citas";
    public static final String ROUTING_KEY_CITAS = "routing.key.citas";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE_NAME);
    }

    @Bean
    Queue queueProcesarCitas() {
        return new Queue(QUEUE_PROCESAR_CITAS_NAME, true);
    }

    @Bean
    Binding bindingProcesarCitas(Queue queueProcesarCitas, DirectExchange directExchange) {
        return BindingBuilder.bind(queueProcesarCitas).to(directExchange).with(ROUTING_KEY_CITAS);
    }

    public static final String FANOUT_EXCHANGE_EVENTOS_CITAS_NAME = "citas.fanout.exchange.eventos";
    public static final String QUEUE_NOTIFICACIONES_NAME = "cola_notificaciones";
    public static final String QUEUE_ESTADISTICAS_NAME = "cola_estadisticas";

    @Bean
    FanoutExchange fanoutExchangeEventosCitas() {
        return new FanoutExchange(FANOUT_EXCHANGE_EVENTOS_CITAS_NAME);
    }

    @Bean
    Queue queueNotificaciones() {
        return new Queue(QUEUE_NOTIFICACIONES_NAME, true);
    }

    @Bean
    Queue queueEstadisticas() {
        return new Queue(QUEUE_ESTADISTICAS_NAME, true);
    }

    @Bean
    Binding bindingNotificaciones(Queue queueNotificaciones, FanoutExchange fanoutExchangeEventosCitas) {
        return BindingBuilder.bind(queueNotificaciones).to(fanoutExchangeEventosCitas);
    }

    @Bean
    Binding bindingEstadisticas(Queue queueEstadisticas, FanoutExchange fanoutExchangeEventosCitas) {
        return BindingBuilder.bind(queueEstadisticas).to(fanoutExchangeEventosCitas);
    }
}
