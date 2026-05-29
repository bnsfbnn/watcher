package com.codex.watcher.dashboard.config;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardConfigRepository extends JpaRepository<DashboardConfig, Long> {
    List<DashboardConfig> findByOwnerOrderByUpdatedAtDesc(String owner);
}
