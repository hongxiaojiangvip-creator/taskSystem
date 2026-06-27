package com.fitness.checkin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.checkin.dto.LoginDTO;
import com.fitness.checkin.entity.User;
import com.fitness.checkin.mapper.UserMapper;
import com.fitness.checkin.util.JwtUtil;
import com.fitness.checkin.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录鉴权服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WechatService wechatService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public LoginVO login(LoginDTO dto) {
        String openid = wechatService.getOpenid(dto.getCode());

        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getOpenid, openid));

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "健身达人");
            user.setAvatar(dto.getAvatar());
            user.setCurrentStreak(0);
            user.setMaxStreak(0);
            user.setTotalDays(0);
            userMapper.insert(user);
        } else {
            // 更新最新授权的昵称头像
            boolean changed = false;
            if (dto.getNickname() != null && !dto.getNickname().equals(user.getNickname())) {
                user.setNickname(dto.getNickname());
                changed = true;
            }
            if (dto.getAvatar() != null && !dto.getAvatar().equals(user.getAvatar())) {
                user.setAvatar(dto.getAvatar());
                changed = true;
            }
            if (changed) {
                userMapper.updateById(user);
            }
        }

        String token = jwtUtil.generate(user.getId());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(user);
        return vo;
    }
}
