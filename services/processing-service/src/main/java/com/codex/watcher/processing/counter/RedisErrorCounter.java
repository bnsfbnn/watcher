package com.codex.watcher.processing.counter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import com.codex.watcher.processing.ProcessingProperties;
import com.codex.watcher.processing.storage.LogDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RedisErrorCounter {
    private static final Logger log = LoggerFactory.getLogger(RedisErrorCounter.class);

    private final StringRedisTemplate redisTemplate;
    private final ProcessingProperties properties;

    public RedisErrorCounter(StringRedisTemplate redisTemplate, ProcessingProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public long increment(LogDocument document) {
        try {
            String serviceName = StringUtils.hasText(document.getServiceName())
                    ? document.getServiceName()
                    : "unknown-service";
            Instant minute = document.getTimestamp() == null
                    ? Instant.now().truncatedTo(ChronoUnit.MINUTES)
                    : document.getTimestamp().truncatedTo(ChronoUnit.MINUTES);
            String key = "watcher:error-count:" + serviceName + ":" + minute.toString();
            Long count = redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, properties.getCounterTtlSeconds(), TimeUnit.SECONDS);
            return count == null ? 0L : count.longValue();
        } catch (Exception ex) {
            log.warn("Failed to update Redis error counter", ex);
            return 0L;
        }
    }
}
