package com.fitness.checkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    /** wx.login 获取的 code */
    @NotBlank(message = "code 不能为空")
    private String code;
    /** 可选:用户授权的昵称 */
    private String nickname;
    /** 可选:用户授权的头像 */
    private String avatar;
}
