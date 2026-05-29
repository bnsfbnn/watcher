package com.codex.watcher.query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.codex.watcher.query.storage.LogDocument;
import com.codex.watcher.query.storage.LogSearchRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LogQueryController {
    private final ElasticsearchOperations operations;
    private final LogSearchRepository repository;

    public LogQueryController(ElasticsearchOperations operations, LogSearchRepository repository) {
        this.operations = operations;
        this.repository = repository;
    }

    @GetMapping("/logs")
    public Map<String, Object> search(
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(buildQuery(level, serviceName, message, from, to))
                .withPageable(PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by(Sort.Direction.DESC, "timestamp")))
                .build();

        SearchHits<LogDocument> hits = operations.search(query, LogDocument.class);
        List<LogDocument> content = new ArrayList<LogDocument>();
        for (SearchHit<LogDocument> hit : hits) {
            content.add(hit.getContent());
        }

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("total", hits.getTotalHits());
        response.put("items", content);
        return response;
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<LogDocument> findById(@PathVariable("id") String id) {
        Optional<LogDocument> document = repository.findById(id);
        return document.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/metrics/summary")
    public Map<String, Object> summary(@RequestParam(value = "size", defaultValue = "1000") int size) {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(PageRequest.of(0, normalizeSummarySize(size), Sort.by(Sort.Direction.DESC, "timestamp")))
                .build();

        SearchHits<LogDocument> hits = operations.search(query, LogDocument.class);
        Map<String, Long> byLevel = new LinkedHashMap<String, Long>();
        Map<String, Long> byService = new LinkedHashMap<String, Long>();

        for (SearchHit<LogDocument> hit : hits) {
            LogDocument document = hit.getContent();
            increment(byLevel, valueOrUnknown(document.getLevel()).toUpperCase(Locale.ROOT));
            increment(byService, valueOrUnknown(document.getServiceName()));
        }

        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("sampleSize", hits.getSearchHits().size());
        response.put("byLevel", byLevel);
        response.put("byService", byService);
        return response;
    }

    private QueryBuilder buildQuery(String level, String serviceName, String message, Instant from, Instant to) {
        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        boolean hasFilter = false;

        if (StringUtils.hasText(level)) {
            bool.filter(QueryBuilders.termQuery("level", level.toUpperCase(Locale.ROOT)));
            hasFilter = true;
        }
        if (StringUtils.hasText(serviceName)) {
            bool.filter(QueryBuilders.termQuery("serviceName", serviceName));
            hasFilter = true;
        }
        if (StringUtils.hasText(message)) {
            bool.must(QueryBuilders.matchQuery("message", message));
            hasFilter = true;
        }
        if (from != null || to != null) {
            org.elasticsearch.index.query.RangeQueryBuilder range = QueryBuilders.rangeQuery("timestamp");
            if (from != null) {
                range.gte(from.toString());
            }
            if (to != null) {
                range.lte(to.toString());
            }
            bool.filter(range);
            hasFilter = true;
        }

        return hasFilter ? bool : QueryBuilders.matchAllQuery();
    }

    private int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 200);
    }

    private int normalizeSummarySize(int size) {
        return Math.min(Math.max(size, 1), 5000);
    }

    private void increment(Map<String, Long> map, String key) {
        Long current = map.get(key);
        map.put(key, current == null ? 1L : current.longValue() + 1L);
    }

    private String valueOrUnknown(String value) {
        return StringUtils.hasText(value) ? value : "unknown";
    }
}
