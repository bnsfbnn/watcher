package com.codex.watcher.ingestion;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.concurrent.ListenableFuture;

@RestController
public class LogIngestionController {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IngestionProperties properties;

    public LogIngestionController(KafkaTemplate<String, String> kafkaTemplate, IngestionProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @PostMapping({"/log", "/api/v1/ingest/logs"})
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestBody String payload,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {

        if (!isAuthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.<String, Object>singletonMap("error", "invalid_api_key"));
        }

        if (!StringUtils.hasText(payload)) {
            return ResponseEntity.badRequest()
                    .body(Collections.<String, Object>singletonMap("error", "empty_payload"));
        }

        try {
            ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(properties.getTopic(), payload);
            SendResult<String, String> result = future.get(properties.getPublishTimeoutMs(), TimeUnit.MILLISECONDS);
            RecordMetadata metadata = result.getRecordMetadata();

            return ResponseEntity.accepted().body(new AcceptedLogResponse(
                    properties.getTopic(),
                    metadata.partition(),
                    metadata.offset()
            ).asMap());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Collections.<String, Object>singletonMap("error", "kafka_publish_failed"));
        }
    }

    private boolean isAuthorized(String apiKey) {
        String expected = properties.getApiKey();
        return !StringUtils.hasText(expected) || expected.equals(apiKey);
    }
}
