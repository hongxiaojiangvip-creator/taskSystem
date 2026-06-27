package com.fitness.checkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckinCreateDTO {
    /** 运动类型ID */
    @NotNull(message = "请选择运动类型")
    private Long sportTypeId;
    /** 运动时长(分钟) */
    @NotNull(message = "请填写运动时长")
    private Integer duration;
    /** 备注 */
    private String remark;
    /** 图片URL列表(先调用 /api/upload 拿到 url) */
    private List<String> images;
}
