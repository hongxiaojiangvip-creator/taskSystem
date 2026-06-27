package com.fitness.checkin.service;

import com.fitness.checkin.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 打卡统计纯逻辑单元测试(不依赖数据库 / 网络 / API Key)。
 */
class CheckinServiceTest {

    private User userWith(LocalDate last, int currentStreak) {
        User u = new User();
        u.setLastCheckinDate(last);
        u.setCurrentStreak(currentStreak);
        return u;
    }

    // ---------- computeStats:连续/最长/累计 ----------

    @Test
    void emptyDates() {
        assertArrayEquals(new int[]{0, 0, 0}, CheckinService.computeStats(Collections.emptyList()));
    }

    @Test
    void singleDay() {
        List<LocalDate> dates = List.of(LocalDate.of(2026, 6, 27));
        assertArrayEquals(new int[]{1, 1, 1}, CheckinService.computeStats(dates));
    }

    @Test
    void consecutiveDays() {
        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2026, 6, 25),
                LocalDate.of(2026, 6, 26),
                LocalDate.of(2026, 6, 27));
        // 当前连续3,最长3,累计3
        assertArrayEquals(new int[]{3, 3, 3}, CheckinService.computeStats(dates));
    }

    @Test
    void brokenThenResumed() {
        // 1,2 号连续(段长2),中断,6,7,8 号连续(段长3,也是结尾段)
        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 6),
                LocalDate.of(2026, 6, 7),
                LocalDate.of(2026, 6, 8));
        // 当前连续=结尾段3,最长=3,累计=5
        assertArrayEquals(new int[]{3, 3, 5}, CheckinService.computeStats(dates));
    }

    @Test
    void maxStreakInMiddle() {
        // 最长段在中间(1-4 连续4天),结尾只有1天
        List<LocalDate> dates = Arrays.asList(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                LocalDate.of(2026, 6, 3),
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 6, 20));
        // 当前连续=1,最长=4,累计=5
        assertArrayEquals(new int[]{1, 4, 5}, CheckinService.computeStats(dates));
    }

    // ---------- effectiveStreak:断签归零 ----------

    @Test
    void streakActiveWhenLastIsToday() {
        User u = userWith(LocalDate.now(), 5);
        assertEquals(5, CheckinService.effectiveStreak(u));
    }

    @Test
    void streakActiveWhenLastIsYesterday() {
        User u = userWith(LocalDate.now().minusDays(1), 5);
        assertEquals(5, CheckinService.effectiveStreak(u));
    }

    @Test
    void streakBrokenWhenLastIsTwoDaysAgo() {
        User u = userWith(LocalDate.now().minusDays(2), 5);
        assertEquals(0, CheckinService.effectiveStreak(u), "断签两天应归零");
    }

    @Test
    void streakZeroWhenNeverCheckedIn() {
        assertEquals(0, CheckinService.effectiveStreak(userWith(null, 0)));
        assertEquals(0, CheckinService.effectiveStreak(null));
    }
}
