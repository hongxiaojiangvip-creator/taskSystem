package com.fitness.checkin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.checkin.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 微信内容安全审核:文字 msgSecCheck、图片 imgSecCheck。
 * 未配置 AppID/Secret(本地 mock 模式)时全部跳过,方便本地联调。
 *
 * 说明:上线前必须配置真实凭证,否则 UGC(备注、图片)无法过审,有封号风险。
 */
@Slf4j
@Service
public class WechatSecurityService {

    @Value("${app.wechat.app-id}")
    private String appId;

    @Value("${app.wechat.app-secret}")
    private String appSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 简单的内存缓存 access_token(有效期 7200s,这里提前到 7000s 刷新)
    private volatile String cachedToken;
    private volatile long tokenExpireAt;

    private boolean enabled() {
        return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank();
    }

    private synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpireAt) {
            return cachedToken;
        }
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, appSecret);
        try {
            JsonNode node = objectMapper.readTree(restTemplate.getForObject(url, String.class));
            if (node.has("access_token")) {
                cachedToken = node.get("access_token").asText();
                tokenExpireAt = now + 7000_000L;
                return cachedToken;
            }
            log.error("获取微信 access_token 失败: {}", node);
        } catch (Exception e) {
            log.error("获取微信 access_token 异常", e);
        }
        return null;
    }

    /**
     * 文字内容安全检测。命中违规抛出业务异常。
     */
    public void checkText(String content) {
        if (!enabled() || content == null || content.isBlank()) {
            return;
        }
        String token = getAccessToken();
        if (token == null) {
            return; // 拿不到 token 时不阻断业务,仅记录日志
        }
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=" + token;
        try {
            // version=2 需要 openid 与场景,这里用 1.0 文本快速校验
            Map<String, Object> body = Map.of("content", content);
            JsonNode node = objectMapper.readTree(restTemplate.postForObject(url, body, String.class));
            int errcode = node.path("errcode").asInt(0);
            if (errcode == 87014) {
                throw new BusinessException("内容含有违规信息,请修改后再提交");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("文字内容审核调用异常,放行", e);
        }
    }

    /**
     * 图片内容安全检测(简化:按 URL 提示,真实场景应上传二进制)。
     * 这里仅占位,记录日志;如需严格校验,需要把图片字节 multipart 上传到 img_sec_check。
     */
    public void checkImage(String imageUrl) {
        if (!enabled() || imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        // 图片审核需要以 multipart 形式上传原始字节;此处留出扩展点,
        // 生产实现可在上传阶段(UploadController)直接对字节调用 img_sec_check。
        log.debug("[img-sec-check] 待接入图片审核: {}", imageUrl);
    }
}
