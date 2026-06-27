package com.fitness.checkin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.checkin.common.BusinessException;
import com.fitness.checkin.dto.CheckinCreateDTO;
import com.fitness.checkin.entity.Checkin;
import com.fitness.checkin.entity.CheckinImage;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        LocalDate today = LocalDate.now();

        // 是否今天的第一次打卡(用于 streak/累计统计)
        boolean firstToday = checkinMapper.selectCount(
                Wrappers.<Checkin>lambdaQuery()
                        .eq(Checkin::getUserId, userId)
                        .eq(Checkin::getCheckinDate, today)) == 0;

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

        if (firstToday) {
            updateStreak(userId, today);
        }

        return toVO(checkin, userId);
    }

    /** 更新用户连续打卡天数等统计 */
    private void updateStreak(Long userId, LocalDate today) {
        User user = userMapper.selectById(userId);
        LocalDate last = user.getLastCheckinDate();
        int current;
        if (last != null && last.plusDays(1).isEqual(today)) {
            current = user.getCurrentStreak() + 1;
        } else if (last != null && last.isEqual(today)) {
            current = user.getCurrentStreak();
        } else {
            current = 1;
        }
        user.setCurrentStreak(current);
        user.setMaxStreak(Math.max(user.getMaxStreak() == null ? 0 : user.getMaxStreak(), current));
        user.setTotalDays((user.getTotalDays() == null ? 0 : user.getTotalDays()) + 1);
        user.setLastCheckinDate(today);
        userMapper.updateById(user);
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
        return result.convert(c -> toVO(c, userId));
    }

    /** 广场(所有人的打卡,分页),用于社交 */
    public IPage<CheckinVO> square(Long userId, int page, int size) {
        Page<Checkin> p = new Page<>(page, size);
        IPage<Checkin> result = checkinMapper.selectPage(p,
                Wrappers.<Checkin>lambdaQuery()
                        .orderByDesc(Checkin::getCreatedAt));
        return result.convert(c -> toVO(c, userId));
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
        // 说明:为简化逻辑,删除单条记录不回滚 streak 统计
    }

    private CheckinVO toVO(Checkin c, Long currentUserId) {
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

        User u = userMapper.selectById(c.getUserId());
        if (u != null) {
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
        }

        List<String> images = checkinImageMapper.selectList(
                        Wrappers.<CheckinImage>lambdaQuery()
                                .eq(CheckinImage::getCheckinId, c.getId())
                                .orderByAsc(CheckinImage::getSort))
                .stream().map(CheckinImage::getUrl).collect(Collectors.toList());
        vo.setImages(images == null ? Collections.emptyList() : images);

        Long likeCnt = likeRecordMapper.selectCount(
                Wrappers.<com.fitness.checkin.entity.LikeRecord>lambdaQuery()
                        .eq(com.fitness.checkin.entity.LikeRecord::getCheckinId, c.getId())
                        .eq(com.fitness.checkin.entity.LikeRecord::getUserId, currentUserId));
        vo.setLiked(likeCnt != null && likeCnt > 0);
        return vo;
    }
}
