package com.fitness.checkin.util;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的内存滑动窗口限流器(单机版,多实例可换 Redis)。
 * key 通常用 "业务:userId",窗口固定 1 分钟。
 */
@Component
public class RateLimiter {

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    /** 当前时间版本,业务调用它 */
    public boolean allow(String key, int maxPerMinute) {
        return allow(key, maxPerMinute, System.currentTimeMillis());
    }

    /** 可注入时间的版本,便于单元测试 */
    public boolean allow(String key, int maxPerMinute, long nowMillis) {
        Deque<Long> q = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && nowMillis - q.peekFirst() >= 60_000L) {
                q.pollFirst();
            }
            if (q.size() >= maxPerMinute) {
                return false;
            }
            q.addLast(nowMillis);
            return true;
        }
    }
}
