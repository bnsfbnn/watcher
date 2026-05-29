package com.codex.watcher.processing.storage;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends ElasticsearchRepository<LogDocument, String> {
}
