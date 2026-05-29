package com.codex.watcher.alerting;

import java.util.Collections;
import java.util.Map;

import com.codex.watcher.contracts.AlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class AlertNotificationHandler {
    private static final Logger log = LoggerFactory.getLogger(AlertNotificationHandler.class);

    private final AlertingProperties properties;
    private final RestTemplate restTemplate;

    public AlertNotificationHandler(AlertingProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @RabbitListener(queues = "${watcher.alerting.queue:alerts_queue}")
    public void handle(AlertMessage message) {
        String text = format(message);
        log.warn("ALERT {}", text);
        sendSlack(text);
        sendEmailMock(text);
    }

    private void sendSlack(String text) {
        if (!StringUtils.hasText(properties.getSlackWebhookUrl())) {
            return;
        }
        try {
            Map<String, String> payload = Collections.singletonMap("text", text);
            restTemplate.postForEntity(properties.getSlackWebhookUrl(), payload, String.class);
        } catch (Exception ex) {
            log.warn("Failed to send Slack alert", ex);
        }
    }

    private void sendEmailMock(String text) {
        if (!"console".equalsIgnoreCase(properties.getEmailSink())) {
            return;
        }
        log.info("EMAIL_MOCK {}", text);
    }

    private String format(AlertMessage message) {
        return "reason=" + message.getReason()
                + " service=" + message.getServiceName()
                + " level=" + message.getLevel()
                + " count=" + message.getObservedCount()
                + " threshold=" + message.getThreshold()
                + " message=\"" + message.getMessage() + "\"";
    }
}
