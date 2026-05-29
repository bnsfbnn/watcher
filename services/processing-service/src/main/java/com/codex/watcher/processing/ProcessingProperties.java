package com.codex.watcher.processing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "watcher.processing")
public class ProcessingProperties {
    private String rawTopic = "raw_logs";
    private boolean dropDebug = true;
    private long errorAlertThreshold = 100L;
    private int counterTtlSeconds = 180;
    private String alertExchange = "alerts_exchange";
    private String alertQueue = "alerts_queue";
    private String alertRoutingKey = "error_routing_key";

    public String getRawTopic() {
        return rawTopic;
    }

    public void setRawTopic(String rawTopic) {
        this.rawTopic = rawTopic;
    }

    public boolean isDropDebug() {
        return dropDebug;
    }

    public void setDropDebug(boolean dropDebug) {
        this.dropDebug = dropDebug;
    }

    public long getErrorAlertThreshold() {
        return errorAlertThreshold;
    }

    public void setErrorAlertThreshold(long errorAlertThreshold) {
        this.errorAlertThreshold = errorAlertThreshold;
    }

    public int getCounterTtlSeconds() {
        return counterTtlSeconds;
    }

    public void setCounterTtlSeconds(int counterTtlSeconds) {
        this.counterTtlSeconds = counterTtlSeconds;
    }

    public String getAlertExchange() {
        return alertExchange;
    }

    public void setAlertExchange(String alertExchange) {
        this.alertExchange = alertExchange;
    }

    public String getAlertQueue() {
        return alertQueue;
    }

    public void setAlertQueue(String alertQueue) {
        this.alertQueue = alertQueue;
    }

    public String getAlertRoutingKey() {
        return alertRoutingKey;
    }

    public void setAlertRoutingKey(String alertRoutingKey) {
        this.alertRoutingKey = alertRoutingKey;
    }
}
