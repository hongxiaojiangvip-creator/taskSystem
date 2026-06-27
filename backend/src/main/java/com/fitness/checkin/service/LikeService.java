package com.fitness.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.checkin.common.BusinessException;
import com.fitness.checkin.entity.Checkin;
import com.fitness.checkin.entity.LikeRecord;
import com.fitness.checkin.mapper.CheckinMapper;
import com.fitness.checkin.mapper.LikeRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务(社交)
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final CheckinMapper checkinMapper;

    /**
     * 点赞/取消点赞,返回最新点赞数
     */
    @Transactional
    public int toggle(Long userId, Long checkinId) {
        Checkin checkin = checkinMapper.selectById(checkinId);
        if (checkin == null) {
            throw new BusinessException("打卡记录不存在");
        }
        LikeRecord exist = likeRecordMapper.selectOne(
                Wrappers.<LikeRecord>lambdaQuery()
                        .eq(LikeRecord::getCheckinId, checkinId)
                        .eq(LikeRecord::getUserId, userId));
        if (exist == null) {
            LikeRecord lr = new LikeRecord();
            lr.setCheckinId(checkinId);
            lr.setUserId(userId);
            likeRecordMapper.insert(lr);
            checkin.setLikeCount((checkin.getLikeCount() == null ? 0 : checkin.getLikeCount()) + 1);
        } else {
            likeRecordMapper.deleteById(exist.getId());
            checkin.setLikeCount(Math.max(0, (checkin.getLikeCount() == null ? 0 : checkin.getLikeCount()) - 1));
        }
        checkinMapper.updateById(checkin);
        return checkin.getLikeCount();
    }
}
