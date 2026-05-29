package com.codex.watcher.alerting;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "watcher.alerting")
public class AlertingProperties {
    private String exchange = "alerts_exchange";
    private String queue = "alerts_queue";
    private String routingKey = "error_routing_key";
    private String slackWebhookUrl = "";
    private String emailSink = "console";

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getSlackWebhookUrl() {
        return slackWebhookUrl;
    }

    public void setSlackWebhookUrl(String slackWebhookUrl) {
        this.slackWebhookUrl = slackWebhookUrl;
    }

    public String getEmailSink() {
        return emailSink;
    }

    public void setEmailSink(String emailSink) {
        this.emailSink = emailSink;
    }
}
