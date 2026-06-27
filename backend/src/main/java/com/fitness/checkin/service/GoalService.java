package com.fitness.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.checkin.dto.GoalDTO;
import com.fitness.checkin.entity.Goal;
import com.fitness.checkin.mapper.GoalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 目标设置服务
 */
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalMapper goalMapper;

    public Goal get(Long userId) {
        Goal goal = goalMapper.selectOne(Wrappers.<Goal>lambdaQuery().eq(Goal::getUserId, userId));
        if (goal == null) {
            goal = new Goal();
            goal.setUserId(userId);
            goal.setWeeklyDays(5);
            goal.setWeeklyMinutes(150);
        }
        return goal;
    }

    public Goal save(Long userId, GoalDTO dto) {
        Goal goal = goalMapper.selectOne(Wrappers.<Goal>lambdaQuery().eq(Goal::getUserId, userId));
        if (goal == null) {
            goal = new Goal();
            goal.setUserId(userId);
        }
        goal.setWeeklyDays(dto.getWeeklyDays());
        goal.setWeeklyMinutes(dto.getWeeklyMinutes());
        if (goal.getId() == null) {
            goalMapper.insert(goal);
        } else {
            goalMapper.updateById(goal);
        }
        return goal;
    }
}
