package com.fitness.checkin.config;

import com.fitness.checkin.common.BusinessException;
import com.fitness.checkin.util.JwtUtil;
import com.fitness.checkin.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录态拦截器:校验 Authorization: Bearer <token>
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录");
        }
        String token = auth.substring(7);
        try {
            Long userId = jwtUtil.parseUserId(token);
            UserContext.set(userId);
            return true;
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期,请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
