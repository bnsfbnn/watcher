package com.codex.watcher.processing.alert;

import com.codex.watcher.contracts.AlertMessage;
import com.codex.watcher.processing.ProcessingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(AlertPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ProcessingProperties properties;

    public AlertPublisher(RabbitTemplate rabbitTemplate, ProcessingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publish(AlertMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    properties.getAlertExchange(),
                    properties.getAlertRoutingKey(),
                    message
            );
        } catch (Exception ex) {
            log.warn("Failed to publish alert task for log {}", message.getId(), ex);
        }
    }
}
