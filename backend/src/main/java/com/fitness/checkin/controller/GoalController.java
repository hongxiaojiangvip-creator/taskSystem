package com.fitness.checkin.controller;

import com.fitness.checkin.common.Result;
import com.fitness.checkin.dto.GoalDTO;
import com.fitness.checkin.entity.Goal;
import com.fitness.checkin.service.GoalService;
import com.fitness.checkin.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    public Result<Goal> get() {
        return Result.success(goalService.get(UserContext.get()));
    }

    @PostMapping
    public Result<Goal> save(@Valid @RequestBody GoalDTO dto) {
        return Result.success(goalService.save(UserContext.get(), dto));
    }
}
