package com.fitness.checkin.controller;

import com.fitness.checkin.common.Result;
import com.fitness.checkin.service.RankService;
import com.fitness.checkin.vo.RankVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    /** 排行榜 period=week|month|all */
    @GetMapping
    public Result<List<RankVO>> rank(@RequestParam(defaultValue = "week") String period,
                                     @RequestParam(defaultValue = "50") int limit) {
        return Result.success(rankService.rank(period, limit));
    }
}
