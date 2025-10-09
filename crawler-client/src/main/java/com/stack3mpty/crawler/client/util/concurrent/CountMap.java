package com.stack3mpty.crawler.client.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ckai
 * @date 2025/9/30 17:33
 */
public class CountMap<K> {
    private final ConcurrentHashMap<K, AtomicInteger> map = new ConcurrentHashMap<>();

    public int acquire(K key) {
        AtomicInteger count = map.get(key);
        if (count == null) {
            count = map.putIfAbsent(key, new AtomicInteger(0));
            if (count == null) {
                count = map.get(key);
            }
        }
        return count.incrementAndGet();
    }

    public void release(K key) {
        AtomicInteger count = map.get(key);
        if (count != null && count.decrementAndGet() <= 0) {
            map.remove(key, count);
        }
    }
}
