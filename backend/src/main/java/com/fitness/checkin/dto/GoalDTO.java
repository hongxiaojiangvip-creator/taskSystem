package com.fitness.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoalDTO {
    @NotNull(message = "请设置每周目标天数")
    private Integer weeklyDays;
    @NotNull(message = "请设置每周目标分钟")
    private Integer weeklyMinutes;
}
