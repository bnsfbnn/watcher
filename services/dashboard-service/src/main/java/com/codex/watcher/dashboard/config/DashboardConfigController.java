package com.codex.watcher.dashboard.config;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardConfigController {
    private final DashboardConfigRepository repository;
    private final DashboardConfigCache cache;

    public DashboardConfigController(DashboardConfigRepository repository, DashboardConfigCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    @GetMapping
    public List<DashboardConfig> list(@RequestParam(value = "owner", defaultValue = "default") String owner) {
        List<DashboardConfig> cached = cache.get(owner);
        if (!cached.isEmpty()) {
            return cached;
        }
        List<DashboardConfig> dashboards = repository.findByOwnerOrderByUpdatedAtDesc(owner);
        cache.put(owner, dashboards);
        return dashboards;
    }

    @PostMapping
    public DashboardConfig create(@RequestBody DashboardConfigRequest request) {
        DashboardConfig dashboard = new DashboardConfig();
        apply(request, dashboard);
        DashboardConfig saved = repository.save(dashboard);
        cache.evict(saved.getOwner());
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<DashboardConfig> update(@PathVariable("id") Long id, @RequestBody DashboardConfigRequest request) {
        Optional<DashboardConfig> existing = repository.findById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        DashboardConfig dashboard = existing.get();
        String previousOwner = dashboard.getOwner();
        apply(request, dashboard);
        DashboardConfig saved = repository.save(dashboard);
        cache.evict(previousOwner);
        cache.evict(saved.getOwner());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Optional<DashboardConfig> existing = repository.findById(id);
        if (!existing.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(existing.get());
        cache.evict(existing.get().getOwner());
        return ResponseEntity.noContent().build();
    }

    private void apply(DashboardConfigRequest request, DashboardConfig dashboard) {
        dashboard.setOwner(StringUtils.hasText(request.getOwner()) ? request.getOwner() : "default");
        dashboard.setName(StringUtils.hasText(request.getName()) ? request.getName() : "Untitled Dashboard");
        dashboard.setConfigJson(StringUtils.hasText(request.getConfigJson()) ? request.getConfigJson() : "{}");
    }
}
