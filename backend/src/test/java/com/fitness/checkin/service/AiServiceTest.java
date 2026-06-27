package com.fitness.checkin.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 未配置 API Key 时,AI 接口应优雅降级返回兜底文案,且不触碰任何依赖(无需数据库/网络)。
 */
class AiServiceTest {

    /** apiKey 未注入(null)→ enabled()=false,所有依赖传 null 也不会被调用 */
    private final AiService ai = new AiService(null, null, null, null, null);

    @Test
    void disabledByDefault() {
        assertFalse(ai.enabled());
    }

    @Test
    void commentFallback() {
        String s = ai.checkinComment(1L, 1L);
        assertNotNull(s);
        assertTrue(s.contains("坚持"));
    }

    @Test
    void weeklyReportFallback() {
        assertNotNull(ai.weeklyReport(1L, false));
    }

    @Test
    void planFallback() {
        assertTrue(ai.trainingPlan(1L, "减脂").contains("建议"));
    }

    @Test
    void chatFallback() {
        assertNotNull(ai.chat(1L, List.of()));
    }
}
