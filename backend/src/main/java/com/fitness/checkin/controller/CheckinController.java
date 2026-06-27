package com.fitness.checkin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.checkin.common.PageResult;
import com.fitness.checkin.common.Result;
import com.fitness.checkin.dto.CheckinCreateDTO;
import com.fitness.checkin.service.CheckinService;
import com.fitness.checkin.service.LikeService;
import com.fitness.checkin.util.UserContext;
import com.fitness.checkin.vo.CheckinVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;
    private final LikeService likeService;

    /** 创建打卡 */
    @PostMapping
    public Result<CheckinVO> create(@Valid @RequestBody CheckinCreateDTO dto) {
        return Result.success(checkinService.create(UserContext.get(), dto));
    }

    /** 今天是否已打卡 */
    @GetMapping("/today")
    public Result<Map<String, Object>> today() {
        boolean checked = checkinService.checkedToday(UserContext.get());
        return Result.success(Map.of("checked", checked));
    }

    /** 我的打卡列表 */
    @GetMapping("/mine")
    public Result<PageResult<CheckinVO>> mine(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        IPage<CheckinVO> p = checkinService.myList(UserContext.get(), page, size);
        return Result.success(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    /** 打卡广场(社交) */
    @GetMapping("/square")
    public Result<PageResult<CheckinVO>> square(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        IPage<CheckinVO> p = checkinService.square(UserContext.get(), page, size);
        return Result.success(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    /** 日历:某月打卡日期 */
    @GetMapping("/calendar")
    public Result<List<LocalDate>> calendar(@RequestParam int year, @RequestParam int month) {
        return Result.success(checkinService.monthDates(UserContext.get(), year, month));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<CheckinVO> detail(@PathVariable Long id) {
        return Result.success(checkinService.detail(UserContext.get(), id));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkinService.delete(UserContext.get(), id);
        return Result.success();
    }

    /** 点赞/取消点赞 */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id) {
        int count = likeService.toggle(UserContext.get(), id);
        return Result.success(Map.of("likeCount", count));
    }
}
