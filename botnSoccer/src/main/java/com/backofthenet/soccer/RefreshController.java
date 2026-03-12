package com.backofthenet.soccer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class RefreshController {

    private final AtomicLong lastRefresh = new AtomicLong(System.currentTimeMillis());

    public void notifyCacheRefresh() {
        lastRefresh.set(System.currentTimeMillis());
        System.out.println("🔄 Cache refreshed at " + lastRefresh.get());
    }

    @GetMapping("/api/cache-refresh")
    public ResponseEntity<Long> getLastRefresh() {
        return ResponseEntity.ok(lastRefresh.get());
    }

    @GetMapping("/api/force-refresh")
    public ResponseEntity<String> forceRefresh() {
    	return ResponseEntity.ok("Refresh triggered");
    }
}