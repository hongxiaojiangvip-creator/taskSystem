package com.fitness.checkin.controller;

import com.fitness.checkin.common.Result;
import com.fitness.checkin.service.AiService;
import com.fitness.checkin.util.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 能力接口(基于 Claude)
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** AI 教练点评某次打卡 */
    @PostMapping("/comment")
    public Result<Map<String, Object>> comment(@RequestBody CommentDTO dto) {
        String text = aiService.checkinComment(UserContext.get(), dto.getCheckinId());
        return Result.success(Map.of("comment", text));
    }

    /** AI 周报(refresh=true 强制重新生成,否则同一周内走缓存) */
    @GetMapping("/weekly-report")
    public Result<Map<String, Object>> weeklyReport(@RequestParam(defaultValue = "false") boolean refresh) {
        return Result.success(Map.of("report", aiService.weeklyReport(UserContext.get(), refresh)));
    }

    /** AI 训练计划 */
    @PostMapping("/plan")
    public Result<Map<String, Object>> plan(@RequestBody(required = false) PlanDTO dto) {
        String goal = dto == null ? null : dto.getGoal();
        return Result.success(Map.of("plan", aiService.trainingPlan(UserContext.get(), goal)));
    }

    /** AI 健身问答(多轮) */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody ChatDTO dto) {
        return Result.success(Map.of("reply", aiService.chat(UserContext.get(), dto.getMessages())));
    }

    @Data
    public static class CommentDTO {
        private Long checkinId;
    }

    @Data
    public static class PlanDTO {
        private String goal;
    }

    @Data
    public static class ChatDTO {
        private List<Map<String, String>> messages;
    }
}
