package com.fitness.checkin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.checkin.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信开放接口服务:code2session 换取 openid
 */
@Slf4j
@Service
public class WechatService {

    @Value("${app.wechat.app-id}")
    private String appId;

    @Value("${app.wechat.app-secret}")
    private String appSecret;

    @Value("${app.wechat.mock-login}")
    private boolean mockLogin;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 用 code 换取 openid。
     * 未配置 AppID 且开启 mockLogin 时,直接把 code 当作 openid 返回,便于本地联调。
     */
    public String getOpenid(String code) {
        if (mockLogin && (appId == null || appId.isBlank())) {
            log.warn("[mock-login] 未配置微信 AppID,使用 code 作为 openid: {}", code);
            return "mock_" + code;
        }
        String url = String.format(CODE2SESSION_URL, appId, appSecret, code);
        try {
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(resp);
            if (node.has("openid")) {
                return node.get("openid").asText();
            }
            String errmsg = node.path("errmsg").asText("未知错误");
            log.error("微信登录失败: {}", resp);
            throw new BusinessException(401, "微信登录失败: " + errmsg);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new BusinessException(500, "微信登录服务异常");
        }
    }
}
