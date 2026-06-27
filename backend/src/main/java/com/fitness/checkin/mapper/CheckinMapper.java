package com.fitness.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.checkin.entity.Checkin;
import com.fitness.checkin.vo.RankVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckinMapper extends BaseMapper<Checkin> {

    /**
     * 排行榜:统计指定日期区间内每个用户的打卡天数与总时长
     */
    @Select("""
            SELECT u.id AS userId, u.nickname AS nickname, u.avatar AS avatar,
                   COUNT(DISTINCT c.checkin_date) AS days,
                   COALESCE(SUM(c.duration), 0) AS minutes
            FROM checkin c
            JOIN `user` u ON u.id = c.user_id
            WHERE c.checkin_date BETWEEN #{start} AND #{end}
            GROUP BY u.id, u.nickname, u.avatar
            ORDER BY days DESC, minutes DESC
            LIMIT #{limit}
            """)
    List<RankVO> rank(@Param("start") LocalDate start,
                      @Param("end") LocalDate end,
                      @Param("limit") int limit);

    /**
     * 按日期统计某用户区间内每天的运动时长汇总(用于图表)
     */
    @Select("""
            SELECT c.checkin_date AS date, COALESCE(SUM(c.duration), 0) AS minutes,
                   COUNT(*) AS count
            FROM checkin c
            WHERE c.user_id = #{userId} AND c.checkin_date BETWEEN #{start} AND #{end}
            GROUP BY c.checkin_date
            ORDER BY c.checkin_date
            """)
    List<Map<String, Object>> dailyStats(@Param("userId") Long userId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);
}
