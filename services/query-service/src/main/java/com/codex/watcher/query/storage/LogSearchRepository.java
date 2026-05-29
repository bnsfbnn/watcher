package com.codex.watcher.query.storage;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogSearchRepository extends ElasticsearchRepository<LogDocument, String> {
}
