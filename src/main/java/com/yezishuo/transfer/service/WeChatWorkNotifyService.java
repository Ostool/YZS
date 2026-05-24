package com.yezishuo.transfer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeChatWorkNotifyService {

    private static final Logger log = LoggerFactory.getLogger(WeChatWorkNotifyService.class);

    @Value("${wechat.webhook.url:https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=ce598a71-d229-4c65-ac20-7a4e5f09056d}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SEPARATOR = "————————————";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 防重复发送缓存
    private final ConcurrentHashMap<Integer, Long> recentMessages = new ConcurrentHashMap<>();
    private static final int DEDUP_SECONDS = 5;

    /**
     * 发送新增调货记录通知
     */
    public void sendAddNotification(String operator, Map<String, Object> details) {
        try {
            String content = buildAddNotificationContent(operator, details);
            sendTextMessage(content);
        } catch (Exception e) {
            log.error("发送新增调货记录通知异常", e);
        }
    }

    /**
     * 发送删除请求通知
     */
    public void sendDeleteRequestNotification(String requester, Map<String, Object> details) {
        try {
            String content = buildDeleteRequestNotificationContent(requester, details);
            sendTextMessage(content);
        } catch (Exception e) {
            log.error("发送删除请求通知异常", e);
        }
    }

    /**
     * 发送审核结果通知
     */
    public void sendAuditResultNotification(String auditor, Map<String, Object> details, boolean approved, String auditRemark, String applicant) {
        try {
            String content = buildAuditResultNotificationContent(auditor, details, approved, auditRemark, applicant);
            sendTextMessage(content);
        } catch (Exception e) {
            log.error("发送审核结果通知异常", e);
        }
    }

    /**
     * 构建新增调货记录通知内容
     */
    private String buildAddNotificationContent(String operator, Map<String, Object> details) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        StringBuilder sb = new StringBuilder();
        sb.append("📦 叶子说眼镜 · 调货通知\n");
        sb.append("       ").append(time).append("\n");
        sb.append(SEPARATOR).append("\n");
        sb.append("➕ 新增调货记录\n\n");

        putField(sb, "货品", getValue(details, "货品", "货品名称"));
        putField(sb, "数量", getValue(details, "数量"));
        putField(sb, "方向", getValue(details, "方向", "方向明细"));
        putField(sb, "取货人", getValue(details, "取货人"));
        putField(sb, "填写人", getValue(details, "填写人"));

        String remark = getValue(details, "备注");
        if (remark != null && !remark.isEmpty() && !"无".equals(remark)) {
            putField(sb, "备注", remark);
        }

        sb.append(SEPARATOR).append("\n");
        sb.append("发起账号：").append(operator).append("\n");
        sb.append("请及时处理 👆");

        return sb.toString();
    }

    /**
     * 构建删除请求通知内容
     */
    private String buildDeleteRequestNotificationContent(String requester, Map<String, Object> details) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);

        StringBuilder sb = new StringBuilder();
        sb.append("📦 叶子说眼镜 · 调货通知\n");
        sb.append("       ").append(time).append("\n");
        sb.append(SEPARATOR).append("\n");
        sb.append("🗑 请求删除以下调货记录\n\n");

        putField(sb, "货品", getValue(details, "货品"));
        putField(sb, "数量", getValue(details, "数量"));
        putField(sb, "方向明细", getValue(details, "方向明细"));
        putField(sb, "取货时间", getValue(details, "取货时间"));
        putField(sb, "请求删除人", requester);
        putField(sb, "删除原因", getValue(details, "删除原因"));

        sb.append(SEPARATOR).append("\n");
        sb.append("发起账号：").append(requester).append("\n");
        sb.append("请及时处理 👆");

        return sb.toString();
    }

    /**
     * 构建审核结果通知内容
     */
    private String buildAuditResultNotificationContent(String auditor, Map<String, Object> details, boolean approved, String auditRemark, String applicant) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);
        String statusText = approved ? "✅ 删除申请已通过" : "❌ 删除申请未通过";

        StringBuilder sb = new StringBuilder();
        sb.append("📦 叶子说眼镜 · 审核通知\n");
        sb.append("       ").append(time).append("\n");
        sb.append(SEPARATOR).append("\n");
        sb.append(statusText).append("\n\n");

        putField(sb, "货品", getValue(details, "货品"));
        putField(sb, "数量", getValue(details, "数量"));
        putField(sb, "审核人", auditor);
        putField(sb, "审核结果", approved ? "通过" : "不通过");

        if (!approved && auditRemark != null && !auditRemark.isEmpty()) {
            putField(sb, "审核意见", auditRemark);
        }

        sb.append(SEPARATOR).append("\n");
        sb.append("发起账号：").append(applicant).append("\n");
        sb.append("请登录系统查看详情 👆");

        return sb.toString();
    }

    /**
     * 从 Map 中获取值，支持多个 key 备选
     */
    private String getValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                String str = value.toString();
                if (!str.isEmpty() && !"null".equals(str)) {
                    return str;
                }
            }
        }
        return null;
    }

    /**
     * 添加字段到 StringBuilder
     */
    private void putField(StringBuilder sb, String label, Object value) {
        if (value != null && !value.toString().isEmpty() && !"null".equals(value.toString())) {
            sb.append(label).append("：").append(value).append("\n");
        }
    }

    /**
     * 发送文本消息到企微
     */
    private void sendTextMessage(String content) {
        try {
            // 防重复检查
            int hash = content.hashCode();
            Long lastSendTime = recentMessages.get(hash);
            long now = System.currentTimeMillis();

            if (lastSendTime != null && (now - lastSendTime) < DEDUP_SECONDS * 1000) {
                log.info("检测到重复消息，已跳过发送");
                return;
            }

            recentMessages.put(hash, now);
            recentMessages.entrySet().removeIf(entry -> (now - entry.getValue()) > DEDUP_SECONDS * 1000);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("msgtype", "text");

            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            requestBody.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.info("发送企微消息内容: {}", jsonBody);

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            String response = restTemplate.postForObject(webhookUrl, entity, String.class);
            log.info("企微接口返回: {}", response);

        } catch (Exception e) {
            log.error("发送企微消息失败", e);
            e.printStackTrace();
        }
    }
}