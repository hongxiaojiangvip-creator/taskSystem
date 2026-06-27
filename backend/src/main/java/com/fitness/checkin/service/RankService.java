package com.fitness.checkin.service;

import com.fitness.checkin.mapper.CheckinMapper;
import com.fitness.checkin.vo.RankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 排行榜服务
 */
@Service
@RequiredArgsConstructor
public class RankService {

    private final CheckinMapper checkinMapper;

    /**
     * @param period week / month / all
     */
    public List<RankVO> rank(String period, int limit) {
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end = today;
        switch (period == null ? "week" : period) {
            case "month" -> start = today.with(TemporalAdjusters.firstDayOfMonth());
            case "all" -> start = LocalDate.of(2000, 1, 1);
            default -> start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        return checkinMapper.rank(start, end, limit);
    }
}
