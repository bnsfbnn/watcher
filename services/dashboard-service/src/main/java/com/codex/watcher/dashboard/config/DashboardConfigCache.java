package com.codex.watcher.dashboard.config;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.codex.watcher.dashboard.DashboardProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DashboardConfigCache {
    private static final Logger log = LoggerFactory.getLogger(DashboardConfigCache.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final DashboardProperties properties;

    public DashboardConfigCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, DashboardProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public List<DashboardConfig> get(String owner) {
        try {
            String cached = redisTemplate.opsForValue().get(key(owner));
            if (!StringUtils.hasText(cached)) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(cached, new TypeReference<List<DashboardConfig>>() {
            });
        } catch (Exception ex) {
            log.warn("Failed to read dashboard cache for owner={}", owner, ex);
            return Collections.emptyList();
        }
    }

    public void put(String owner, List<DashboardConfig> dashboards) {
        try {
            redisTemplate.opsForValue().set(
                    key(owner),
                    objectMapper.writeValueAsString(dashboards),
                    properties.getCacheTtlSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (Exception ex) {
            log.warn("Failed to write dashboard cache for owner={}", owner, ex);
        }
    }

    public void evict(String owner) {
        try {
            redisTemplate.delete(key(owner));
        } catch (Exception ex) {
            log.warn("Failed to evict dashboard cache for owner={}", owner, ex);
        }
    }

    private String key(String owner) {
        return "watcher:dashboard-configs:" + owner;
    }
}
