package com.codex.watcher.ingestion;

import java.util.LinkedHashMap;
import java.util.Map;

public class AcceptedLogResponse {
    private final String topic;
    private final int partition;
    private final long offset;

    public AcceptedLogResponse(String topic, int partition, long offset) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", "accepted");
        response.put("topic", topic);
        response.put("partition", partition);
        response.put("offset", offset);
        return response;
    }
}
