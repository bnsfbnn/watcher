package com.codex.watcher.processing;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.codex.watcher.contracts.AlertMessage;
import com.codex.watcher.processing.alert.AlertPublisher;
import com.codex.watcher.processing.counter.RedisErrorCounter;
import com.codex.watcher.processing.storage.LogDocument;
import com.codex.watcher.processing.storage.LogEntryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class KafkaLogConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaLogConsumer.class);

    private final ObjectMapper objectMapper;
    private final LogEntryRepository repository;
    private final RedisErrorCounter errorCounter;
    private final AlertPublisher alertPublisher;
    private final ProcessingProperties properties;

    public KafkaLogConsumer(ObjectMapper objectMapper,
                            LogEntryRepository repository,
                            RedisErrorCounter errorCounter,
                            AlertPublisher alertPublisher,
                            ProcessingProperties properties) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.errorCounter = errorCounter;
        this.alertPublisher = alertPublisher;
        this.properties = properties;
    }

    @KafkaListener(topics = "${watcher.processing.raw-topic:raw_logs}", groupId = "${spring.kafka.consumer.group-id:processing-service}")
    public void consumeLog(String payload) {
        try {
            LogDocument document = parse(payload);

            if (properties.isDropDebug() && "DEBUG".equals(document.getLevel())) {
                log.debug("Dropped DEBUG log from {}", document.getServiceName());
                return;
            }

            repository.save(document);
            log.info("Processed log id={} service={} level={}", document.getId(), document.getServiceName(), document.getLevel());

            if ("ERROR".equals(document.getLevel())) {
                long count = errorCounter.increment(document);
                if (count >= properties.getErrorAlertThreshold()) {
                    alertPublisher.publish(toAlert(document, count));
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to process raw log payload={}", payload, ex);
        }
    }

    private LogDocument parse(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        LogDocument document = new LogDocument();
        document.setId(text(root, "id", UUID.randomUUID().toString()));
        document.setTimestamp(timestamp(root));
        document.setLevel(text(root, "level", "INFO").toUpperCase(Locale.ROOT));
        document.setMessage(text(root, "message", payload));
        document.setServiceName(text(root, "serviceName", text(root, "service_name", "unknown-service")));
        document.setTraceId(text(root, "traceId", text(root, "trace_id", null)));
        document.setUserId(text(root, "userId", text(root, "user_id", null)));
        document.setIpAddress(text(root, "ipAddress", text(root, "ip_address", null)));
        document.setEnvironment(text(root, "environment", "local"));
        document.setAttributes(extraAttributes(root));
        return document;
    }

    private String text(JsonNode root, String field, String defaultValue) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text : defaultValue;
    }

    private Instant timestamp(JsonNode root) {
        String raw = text(root, "timestamp", null);
        if (StringUtils.hasText(raw)) {
            try {
                return Instant.parse(raw);
            } catch (Exception ignored) {
                log.debug("Invalid timestamp {}, using current time", raw);
            }
        }
        return Instant.now();
    }

    private Map<String, Object> extraAttributes(JsonNode root) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            if (isKnownField(key)) {
                continue;
            }
            attributes.put(key, objectMapper.convertValue(entry.getValue(), Object.class));
        }
        return attributes;
    }

    private boolean isKnownField(String key) {
        return "id".equals(key)
                || "timestamp".equals(key)
                || "level".equals(key)
                || "message".equals(key)
                || "serviceName".equals(key)
                || "service_name".equals(key)
                || "traceId".equals(key)
                || "trace_id".equals(key)
                || "userId".equals(key)
                || "user_id".equals(key)
                || "ipAddress".equals(key)
                || "ip_address".equals(key)
                || "environment".equals(key);
    }

    private AlertMessage toAlert(LogDocument document, long count) {
        AlertMessage alert = new AlertMessage();
        alert.setId(document.getId());
        alert.setLevel(document.getLevel());
        alert.setMessage(document.getMessage());
        alert.setServiceName(document.getServiceName());
        alert.setTraceId(document.getTraceId());
        alert.setTimestamp(document.getTimestamp());
        alert.setReason("error_threshold_exceeded");
        alert.setObservedCount(count);
        alert.setThreshold(properties.getErrorAlertThreshold());
        return alert;
    }
}
