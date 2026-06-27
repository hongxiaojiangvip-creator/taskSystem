package com.fitness.checkin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.checkin.common.Result;
import com.fitness.checkin.entity.SportType;
import com.fitness.checkin.mapper.SportTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sport-types")
@RequiredArgsConstructor
public class SportTypeController {

    private final SportTypeMapper sportTypeMapper;

    /** 运动类型字典(无需登录) */
    @GetMapping
    public Result<List<SportType>> list() {
        return Result.success(sportTypeMapper.selectList(
                Wrappers.<SportType>lambdaQuery()
                        .eq(SportType::getEnabled, 1)
                        .orderByAsc(SportType::getSort)));
    }
}
