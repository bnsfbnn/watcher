package com.codex.watcher.processing.alert;

import com.codex.watcher.processing.ProcessingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlertRabbitConfiguration {
    @Bean
    public TopicExchange alertsExchange(ProcessingProperties properties) {
        return new TopicExchange(properties.getAlertExchange(), true, false);
    }

    @Bean
    public Queue alertsQueue(ProcessingProperties properties) {
        return new Queue(properties.getAlertQueue(), true);
    }

    @Bean
    public Binding alertsBinding(ProcessingProperties properties, Queue alertsQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(alertsQueue).to(alertsExchange).with(properties.getAlertRoutingKey());
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
