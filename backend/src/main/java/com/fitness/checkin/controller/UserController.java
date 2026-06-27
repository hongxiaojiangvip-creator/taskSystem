package com.fitness.checkin.controller;

import com.fitness.checkin.common.Result;
import com.fitness.checkin.entity.User;
import com.fitness.checkin.mapper.UserMapper;
import com.fitness.checkin.util.UserContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    /** 当前用户信息 */
    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userMapper.selectById(UserContext.get()));
    }

    /** 更新昵称/头像 */
    @PostMapping("/profile")
    public Result<User> updateProfile(@RequestBody ProfileDTO dto) {
        User user = userMapper.selectById(UserContext.get());
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        userMapper.updateById(user);
        return Result.success(user);
    }

    @Data
    public static class ProfileDTO {
        private String nickname;
        private String avatar;
    }
}
