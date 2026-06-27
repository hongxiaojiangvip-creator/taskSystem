package com.fitness.checkin.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlock;
import com.fitness.checkin.common.BusinessException;
import com.fitness.checkin.entity.Checkin;
import com.fitness.checkin.entity.Goal;
import com.fitness.checkin.entity.User;
import com.fitness.checkin.mapper.CheckinMapper;
import com.fitness.checkin.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 能力服务,基于 Anthropic Claude。
 * 未配置 ANTHROPIC_API_KEY 时返回友好的降级文案,保证小程序仍可用。
 */
@Slf4j
@Service
public class AiService {

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    private final CheckinMapper checkinMapper;
    private final UserMapper userMapper;
    private final StatsService statsService;
    private final GoalService goalService;

    private volatile AnthropicClient client;

    public AiService(CheckinMapper checkinMapper, UserMapper userMapper,
                     StatsService statsService, GoalService goalService) {
        this.checkinMapper = checkinMapper;
        this.userMapper = userMapper;
        this.statsService = statsService;
        this.goalService = goalService;
    }

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    private AnthropicClient client() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
                }
            }
        }
        return client;
    }

    /** 单轮文本补全 */
    private String complete(String system, String userPrompt, long maxTokens) {
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(maxTokens)
                    .system(system)
                    .addUserMessage(userPrompt)
                    .build();
            Message response = client().messages().create(params);
            return extractText(response);
        } catch (Exception e) {
            log.error("调用 Claude 失败", e);
            throw new BusinessException(500, "AI 服务暂时不可用,请稍后再试");
        }
    }

    private String extractText(Message response) {
        return response.content().stream()
                .flatMap(b -> b.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();
    }

    // ============ 业务能力 ============

    private static final String COACH_SYSTEM =
            "你是一名专业、热情的健身教练。请用简体中文、口语化、积极正向的语气回复," +
            "给用户的打卡做点评和鼓励,并给出 1 条具体可执行的小建议。" +
            "回复控制在 60 字以内,可以适当使用 emoji,不要列条目。";

    /** AI 教练对单次打卡的点评 */
    public String checkinComment(Long userId, Long checkinId) {
        if (!enabled()) {
            return "坚持就是胜利,今天也很棒!继续保持哦 💪";
        }
        Checkin c = checkinMapper.selectById(checkinId);
        if (c == null) {
            throw new BusinessException("打卡记录不存在");
        }
        User user = userMapper.selectById(userId);
        int streak = CheckinService.effectiveStreak(user);
        String prompt = String.format(
                "用户刚完成一次健身打卡。运动类型:%s;时长:%d 分钟;估算消耗:%d 千卡;" +
                "备注:%s;当前已连续打卡 %d 天,累计 %d 天。请点评并鼓励他。",
                c.getSportName(), c.getDuration(), c.getCalorie(),
                (c.getRemark() == null || c.getRemark().isBlank()) ? "无" : c.getRemark(),
                streak, user.getTotalDays() == null ? 0 : user.getTotalDays());
        return complete(COACH_SYSTEM, prompt, 400);
    }

    /** AI 周报:分析最近 7 天运动数据并给建议 */
    public String weeklyReport(Long userId) {
        if (!enabled()) {
            return "本周继续保持规律运动,注意循序渐进,记得拉伸和补水哦~";
        }
        Map<String, Object> overview = statsService.overview(userId);
        List<Map<String, Object>> trend = statsService.trend(userId, 7);
        String trendStr = trend.stream()
                .map(t -> t.get("date") + ":" + t.get("minutes") + "分钟")
                .collect(Collectors.joining(", "));
        String system = "你是一名专业健身教练兼数据分析师。请用简体中文输出一段健身周报," +
                "包含:本周表现总结、值得肯定的地方、需要改进的地方、下周的 2-3 条具体建议。" +
                "语气积极鼓励,结构清晰,可分小段,总字数 200 字以内。";
        String prompt = String.format(
                "用户本周健身数据:连续打卡 %s 天,累计 %s 天,本周打卡 %s 天、共 %s 分钟," +
                "每周目标 %s 天 / %s 分钟。最近 7 天每日运动时长:%s。请生成周报。",
                overview.get("currentStreak"), overview.get("totalDays"),
                overview.get("weekDays"), overview.get("weekMinutes"),
                overview.get("goalDays"), overview.get("goalMinutes"), trendStr);
        return complete(system, prompt, 1200);
    }

    /** AI 个性化训练计划 */
    public String trainingPlan(Long userId, String goalText) {
        if (!enabled()) {
            return "建议:每周运动 3-5 天,有氧与力量结合,每次 30-45 分钟,逐步增加强度。";
        }
        Map<String, Object> overview = statsService.overview(userId);
        Goal goal = goalService.get(userId);
        String system = "你是一名专业健身教练。请用简体中文为用户制定一份未来一周的训练计划," +
                "按周一到周日列出每天的安排(包括休息日),每天注明运动类型、时长和强度提示。" +
                "结合用户当前水平,循序渐进,安全第一。结构清晰,可用列表,总字数 300 字以内。";
        String prompt = String.format(
                "用户当前情况:累计打卡 %s 天,本周已打卡 %s 天、%s 分钟,每周目标 %s 天 / %s 分钟。" +
                "用户的额外目标/诉求:%s。请制定一周训练计划。",
                overview.get("totalDays"), overview.get("weekDays"), overview.get("weekMinutes"),
                goal.getWeeklyDays(), goal.getWeeklyMinutes(),
                (goalText == null || goalText.isBlank()) ? "无特别说明,常规健身" : goalText);
        return complete(system, prompt, 1500);
    }

    /** AI 健身问答助手(支持多轮) */
    public String chat(List<Map<String, String>> messages) {
        if (!enabled()) {
            return "AI 助手暂未开启,请联系管理员配置后使用~";
        }
        try {
            String system = "你是一名专业、友善的健身与健康顾问。请用简体中文回答用户关于健身、" +
                    "饮食、康复、作息等问题。回答专业、简洁、实用。涉及伤病或医疗问题时,提醒用户就医。";
            MessageCreateParams.Builder builder = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(1024)
                    .system(system);
            for (Map<String, String> m : messages) {
                String role = m.getOrDefault("role", "user");
                String content = m.getOrDefault("content", "");
                if ("assistant".equals(role)) {
                    builder.addAssistantMessage(content);
                } else {
                    builder.addUserMessage(content);
                }
            }
            return extractText(client().messages().create(builder.build()));
        } catch (Exception e) {
            log.error("AI 对话失败", e);
            throw new BusinessException(500, "AI 服务暂时不可用,请稍后再试");
        }
    }
}
