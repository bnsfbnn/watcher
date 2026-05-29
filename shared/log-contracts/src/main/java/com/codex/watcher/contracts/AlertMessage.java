package com.codex.watcher.contracts;

import java.io.Serializable;
import java.time.Instant;

public class AlertMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String level;
    private String message;
    private String serviceName;
    private String traceId;
    private Instant timestamp;
    private String reason;
    private long observedCount;
    private long threshold;

    public AlertMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getObservedCount() {
        return observedCount;
    }

    public void setObservedCount(long observedCount) {
        this.observedCount = observedCount;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }
}
