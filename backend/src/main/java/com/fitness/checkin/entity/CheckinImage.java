package com.fitness.checkin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("checkin_image")
public class CheckinImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long checkinId;
    private String url;
    private Integer sort;
}
