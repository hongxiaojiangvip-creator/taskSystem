package com.fitness.checkin.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CheckinVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private LocalDate checkinDate;
    private Long sportTypeId;
    private String sportName;
    private Integer duration;
    private Integer calorie;
    private String remark;
    private Integer likeCount;
    private Boolean liked;
    private List<String> images;
    private LocalDateTime createdAt;
}
