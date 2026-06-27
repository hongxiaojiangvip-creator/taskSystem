package com.fitness.checkin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.checkin.common.BusinessException;
import com.fitness.checkin.dto.CheckinCreateDTO;
import com.fitness.checkin.entity.Checkin;
import com.fitness.checkin.entity.CheckinImage;
import com.fitness.checkin.entity.LikeRecord;
import com.fitness.checkin.entity.SportType;
import com.fitness.checkin.entity.User;
import com.fitness.checkin.mapper.CheckinImageMapper;
import com.fitness.checkin.mapper.CheckinMapper;
import com.fitness.checkin.mapper.LikeRecordMapper;
import com.fitness.checkin.mapper.SportTypeMapper;
import com.fitness.checkin.mapper.UserMapper;
import com.fitness.checkin.vo.CheckinVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 打卡核心服务
 */
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinMapper checkinMapper;
    private final CheckinImageMapper checkinImageMapper;
    private final SportTypeMapper sportTypeMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final WechatSecurityService securityService;

    /** 各运动类型每分钟估算卡路里 */
    private static final Map<String, Integer> CALORIE_PER_MIN = Map.of(
            "跑步", 11, "健身", 8, "瑜伽", 4, "骑行", 8,
            "游泳", 10, "球类", 9, "徒步", 6, "其他", 6);

    @Transactional
    public CheckinVO create(Long userId, CheckinCreateDTO dto) {
        SportType type = sportTypeMapper.selectById(dto.getSportTypeId());
        if (type == null) {
            throw new BusinessException("运动类型不存在");
        }
        // 文字内容安全审核(图片已在上传阶段审核),未配置微信凭证时自动跳过
        securityService.checkText(dto.getRemark());

        LocalDate today = LocalDate.now();

        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setSportTypeId(type.getId());
        checkin.setSportName(type.getName());
        checkin.setDuration(dto.getDuration() == null ? 0 : dto.getDuration());
        checkin.setCalorie(estimateCalorie(type.getName(), checkin.getDuration()));
        checkin.setRemark(dto.getRemark());
        checkin.setLikeCount(0);
        checkinMapper.insert(checkin);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            int sort = 0;
            for (String url : dto.getImages()) {
                CheckinImage img = new CheckinImage();
                img.setCheckinId(checkin.getId());
                img.setUrl(url);
                img.setSort(sort++);
                checkinImageMapper.insert(img);
            }
        }

        // 统一由打卡记录重算用户统计,保证 streak / 累计天数始终正确
        recomputeUserStats(userId);

        return toVO(checkin, userId);
    }

    /**
     * 根据打卡记录重算用户的连续天数、最长连续、累计天数、最近打卡日期。
     * 比增量更新更健壮:新增、删除都调用它,不会出现统计漂移。
     */
    public void recomputeUserStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        List<LocalDate> dates = checkinMapper.selectList(
                        Wrappers.<Checkin>lambdaQuery()
                                .select(Checkin::getCheckinDate)
                                .eq(Checkin::getUserId, userId))
                .stream().map(Checkin::getCheckinDate).distinct()
                .sorted().collect(Collectors.toList());

        if (dates.isEmpty()) {
            user.setCurrentStreak(0);
            user.setMaxStreak(0);
            user.setTotalDays(0);
            user.setLastCheckinDate(null);
            userMapper.updateById(user);
            return;
        }

        int[] stats = computeStats(dates);
        user.setCurrentStreak(stats[0]);
        user.setMaxStreak(stats[1]);
        user.setTotalDays(stats[2]);
        user.setLastCheckinDate(dates.get(dates.size() - 1));
        userMapper.updateById(user);
    }

    /**
     * 由「升序、去重」的打卡日期列表计算 {当前连续, 最长连续, 累计天数}。
     * 纯函数,便于单元测试。当前连续 = 以最后一天结尾的连续段长度(是否已断签由 effectiveStreak 判断)。
     */
    static int[] computeStats(List<LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            return new int[]{0, 0, 0};
        }
        int total = dates.size();
        int maxStreak = 1;
        int run = 1;
        for (int i = 1; i < dates.size(); i++) {
            run = dates.get(i - 1).plusDays(1).isEqual(dates.get(i)) ? run + 1 : 1;
            maxStreak = Math.max(maxStreak, run);
        }
        int current = 1;
        for (int i = dates.size() - 1; i > 0; i--) {
            if (dates.get(i - 1).plusDays(1).isEqual(dates.get(i))) {
                current++;
            } else {
                break;
            }
        }
        return new int[]{current, maxStreak, total};
    }

    /**
     * 用于展示的"当前连续天数":只有最近一次打卡在今天或昨天时才算连续,否则已断签为 0。
     * 避免直接读 user.currentStreak 出现过期值。
     */
    public static int effectiveStreak(User user) {
        if (user == null || user.getLastCheckinDate() == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastCheckinDate();
        if (last.isEqual(today) || last.plusDays(1).isEqual(today)) {
            return user.getCurrentStreak() == null ? 0 : user.getCurrentStreak();
        }
        return 0;
    }

    private int estimateCalorie(String sportName, int duration) {
        return CALORIE_PER_MIN.getOrDefault(sportName, 6) * duration;
    }

    /** 今天是否已打卡 */
    public boolean checkedToday(Long userId) {
        return checkinMapper.selectCount(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getUserId, userId)
                        .eq(Checkin::getCheckinDate, LocalDate.now())) > 0;
    }

    /** 我的打卡列表(分页) */
    public IPage<CheckinVO> myList(Long userId, int page, int size) {
        Page<Checkin> p = new Page<>(page, size);
        IPage<Checkin> result = checkinMapper.selectPage(p,
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getUserId, userId)
                        .orderByDesc(Checkin::getCreatedAt));
        return toVOPage(result, userId);
    }

    /** 广场(所有人的打卡,分页),用于社交 */
    public IPage<CheckinVO> square(Long userId, int page, int size) {
        Page<Checkin> p = new Page<>(page, size);
        IPage<Checkin> result = checkinMapper.selectPage(p,
                Wrappers.<Checkin>lambdaQuery()
                        .orderByDesc(Checkin::getCreatedAt));
        return toVOPage(result, userId);
    }

    private IPage<CheckinVO> toVOPage(IPage<Checkin> source, Long userId) {
        Page<CheckinVO> voPage = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        voPage.setRecords(toVOList(source.getRecords(), userId));
        return voPage;
    }

    /** 某月已打卡的日期列表(日历用) */
    public List<LocalDate> monthDates(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return checkinMapper.selectList(
                        Wrappers.<Checkin>lambdaQuery()
                                .select(Checkin::getCheckinDate)
                                .eq(Checkin::getUserId, userId)
                                .between(Checkin::getCheckinDate, start, end))
                .stream().map(Checkin::getCheckinDate).distinct().collect(Collectors.toList());
    }

    public CheckinVO detail(Long userId, Long id) {
        Checkin c = checkinMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("打卡记录不存在");
        }
        return toVO(c, userId);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Checkin c = checkinMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("打卡记录不存在");
        }
        if (!c.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人记录");
        }
        checkinMapper.deleteById(id);
        checkinImageMapper.delete(Wrappers.<CheckinImage>lambdaQuery()
                .eq(CheckinImage::getCheckinId, id));
        // 删除后重算统计,回滚 streak / 累计天数
        recomputeUserStats(userId);
    }

    /** 单条转 VO(详情用) */
    private CheckinVO toVO(Checkin c, Long currentUserId) {
        return toVOList(Collections.singletonList(c), currentUserId).get(0);
    }

    /**
     * 批量转 VO:一次性查出涉及的用户、图片、当前用户的点赞,避免逐条查询(N+1)。
     */
    private List<CheckinVO> toVOList(List<Checkin> checkins, Long currentUserId) {
        if (checkins == null || checkins.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = checkins.stream().map(Checkin::getUserId).collect(Collectors.toSet());
        List<Long> checkinIds = checkins.stream().map(Checkin::getId).collect(Collectors.toList());

        // 批量查用户
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 批量查图片并按 checkinId 分组
        Map<Long, List<String>> imageMap = new HashMap<>();
        checkinImageMapper.selectList(
                        Wrappers.<CheckinImage>lambdaQuery()
                                .in(CheckinImage::getCheckinId, checkinIds)
                                .orderByAsc(CheckinImage::getSort))
                .forEach(img -> imageMap.computeIfAbsent(img.getCheckinId(), k -> new ArrayList<>())
                        .add(img.getUrl()));

        // 批量查当前用户点赞过的 checkinId
        Set<Long> likedIds = new HashSet<>();
        if (currentUserId != null) {
            likeRecordMapper.selectList(
                            Wrappers.<LikeRecord>lambdaQuery()
                                    .eq(LikeRecord::getUserId, currentUserId)
                                    .in(LikeRecord::getCheckinId, checkinIds))
                    .forEach(lr -> likedIds.add(lr.getCheckinId()));
        }

        List<CheckinVO> result = new ArrayList<>(checkins.size());
        for (Checkin c : checkins) {
            CheckinVO vo = new CheckinVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setCheckinDate(c.getCheckinDate());
            vo.setSportTypeId(c.getSportTypeId());
            vo.setSportName(c.getSportName());
            vo.setDuration(c.getDuration());
            vo.setCalorie(c.getCalorie());
            vo.setRemark(c.getRemark());
            vo.setLikeCount(c.getLikeCount());
            vo.setCreatedAt(c.getCreatedAt());

            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            vo.setImages(imageMap.getOrDefault(c.getId(), Collections.emptyList()));
            vo.setLiked(likedIds.contains(c.getId()));
            result.add(vo);
        }
        return result;
    }
}
