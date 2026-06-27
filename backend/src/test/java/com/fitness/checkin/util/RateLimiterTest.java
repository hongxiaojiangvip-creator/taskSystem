package com.fitness.checkin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void allowsWithinLimit() {
        RateLimiter rl = new RateLimiter();
        long t = 1_000_000L;
        assertTrue(rl.allow("k", 3, t));
        assertTrue(rl.allow("k", 3, t));
        assertTrue(rl.allow("k", 3, t));
    }

    @Test
    void blocksOverLimit() {
        RateLimiter rl = new RateLimiter();
        long t = 1_000_000L;
        assertTrue(rl.allow("k", 2, t));
        assertTrue(rl.allow("k", 2, t));
        assertFalse(rl.allow("k", 2, t), "第3次应被限流");
    }

    @Test
    void windowSlidesAfterOneMinute() {
        RateLimiter rl = new RateLimiter();
        long t = 1_000_000L;
        assertTrue(rl.allow("k", 1, t));
        assertFalse(rl.allow("k", 1, t + 30_000L), "30秒内仍受限");
        assertTrue(rl.allow("k", 1, t + 60_000L), "满60秒后窗口滑出,放行");
    }

    @Test
    void keysAreIndependent() {
        RateLimiter rl = new RateLimiter();
        long t = 1_000_000L;
        assertTrue(rl.allow("a", 1, t));
        assertTrue(rl.allow("b", 1, t), "不同 key 互不影响");
    }
}
