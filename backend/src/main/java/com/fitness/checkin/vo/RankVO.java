package com.fitness.checkin.vo;

import lombok.Data;

@Data
public class RankVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer days;
    private Integer minutes;
}
