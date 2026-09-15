package com.abdel.employee_management.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiterService {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Returns true if the action is allowed, false if the caller has exceeded
     * maxAttempts within the given time window for this key.
     */
    public boolean isAllowed(String key, int maxAttempts, Duration window) {
        long now = System.currentTimeMillis();
        Window w = windows.computeIfAbsent(key, k -> new Window(now));

        synchronized (w) {
            if (now - w.windowStart > window.toMillis()) {
                // window expired — reset
                w.windowStart = now;
                w.count.set(0);
            }
            if (w.count.get() >= maxAttempts) {
                return false;
            }
            w.count.incrementAndGet();
            return true;
        }
    }

    private static class Window {
        volatile long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}