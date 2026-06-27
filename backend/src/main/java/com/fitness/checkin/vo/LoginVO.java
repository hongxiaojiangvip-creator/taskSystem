package com.fitness.checkin.vo;

import com.fitness.checkin.entity.User;
import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private User user;
}
