package com.yuezm.project.common

import groovy.transform.CompileStatic
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Function

/**
 * LocalCache
 *
 * @author yzm
 * @version 1.0
 * @description 本地缓存
 * @date 2025/8/12 18:34
 */


@CompileStatic
class LocalCache {
    private static final long CLEANUP_INTERVAL = 1000L // 清理间隔（毫秒）

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>()
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()
    private final AtomicLong cleanupCounter = new AtomicLong()

    LocalCache() {
        scheduler.scheduleAtFixedRate({ -> cleanupExpiredEntries() },
                CLEANUP_INTERVAL,
                CLEANUP_INTERVAL,
                TimeUnit.MILLISECONDS)
    }

    <T> void put(String key, T value, long expireTime, Consumer<T> callBack = null) {
        if (expireTime <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0")
        }
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + expireTime, callBack))
    }

    <T> T get(String key) {
        CacheEntry entry = cache.get(key)
        if (entry == null) {
            return null
        }
        if (entry.isExpired()) {
            cache.remove(key)
            return null
        }
        return (T)entry.value
    }

    void remove(String key) {
        cache.remove(key)
    }

    private void cleanupExpiredEntries() {
        long currentCleanup = cleanupCounter.incrementAndGet()
        cache.forEach { String key, CacheEntry entry ->
            if (entry.isExpired()) {
                cache.remove(key)
                if(entry.callBack){
                    entry.callBack.accept(entry.value)
                }
            }
        }
    }

    void shutdown() {
        scheduler.shutdown()
    }

    private static class CacheEntry<T> {
        final T value
        final long expireTime

        Consumer<T> callBack

        CacheEntry(T value, long expireTime, Consumer<T> callBack) {
            this.value = value
            this.expireTime = expireTime
            this.callBack = callBack
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime
        }
    }
}