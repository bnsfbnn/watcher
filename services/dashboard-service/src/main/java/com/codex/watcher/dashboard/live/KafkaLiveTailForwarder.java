package com.codex.watcher.dashboard.live;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaLiveTailForwarder {
    private static final Logger log = LoggerFactory.getLogger(KafkaLiveTailForwarder.class);

    private final SimpMessagingTemplate messagingTemplate;

    public KafkaLiveTailForwarder(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "${watcher.dashboard.live-topic:raw_logs}", groupId = "${spring.kafka.consumer.group-id:dashboard-live-tail}")
    public void forward(String payload) {
        messagingTemplate.convertAndSend("/topic/logs", payload);
        log.debug("Forwarded log payload to live-tail subscribers");
    }
}
