package com.fitness.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.checkin.entity.Checkin;
import com.fitness.checkin.entity.Goal;
import com.fitness.checkin.entity.User;
import com.fitness.checkin.mapper.CheckinMapper;
import com.fitness.checkin.mapper.GoalMapper;
import com.fitness.checkin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务(图表)
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final CheckinMapper checkinMapper;
    private final UserMapper userMapper;
    private final GoalMapper goalMapper;

    /**
     * 概览:连续天数、累计天数、本周天数/分钟、目标达成率
     */
    public Map<String, Object> overview(Long userId) {
        User user = userMapper.selectById(userId);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Checkin> weekCheckins = checkinMapper.selectList(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getUserId, userId)
                        .between(Checkin::getCheckinDate, weekStart, today));

        long weekDays = weekCheckins.stream().map(Checkin::getCheckinDate).distinct().count();
        int weekMinutes = weekCheckins.stream().mapToInt(c -> c.getDuration() == null ? 0 : c.getDuration()).sum();

        Goal goal = goalMapper.selectOne(Wrappers.<Goal>lambdaQuery().eq(Goal::getUserId, userId));
        int goalDays = goal != null ? goal.getWeeklyDays() : 5;
        int goalMinutes = goal != null ? goal.getWeeklyMinutes() : 150;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("currentStreak", CheckinService.effectiveStreak(user));
        map.put("maxStreak", user.getMaxStreak());
        map.put("totalDays", user.getTotalDays());
        map.put("weekDays", weekDays);
        map.put("weekMinutes", weekMinutes);
        map.put("goalDays", goalDays);
        map.put("goalMinutes", goalMinutes);
        map.put("daysProgress", goalDays == 0 ? 0 : Math.min(100, weekDays * 100 / goalDays));
        map.put("minutesProgress", goalMinutes == 0 ? 0 : Math.min(100, weekMinutes * 100 / goalMinutes));
        return map;
    }

    /**
     * 最近 N 天每天的运动分钟趋势(图表用)
     */
    public List<Map<String, Object>> trend(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        List<Map<String, Object>> raw = checkinMapper.dailyStats(userId, start, end);

        // 转成 date -> minutes 映射
        Map<String, Integer> minutesMap = new LinkedHashMap<>();
        for (Map<String, Object> row : raw) {
            minutesMap.put(String.valueOf(row.get("date")),
                    ((Number) row.getOrDefault("minutes", 0)).intValue());
        }

        // 补全没有打卡的日期为 0
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            String key = d.toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", key);
            item.put("minutes", minutesMap.getOrDefault(key, 0));
            result.add(item);
        }
        return result;
    }
}
