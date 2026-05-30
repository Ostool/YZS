package com.yezishuo.transfer.controller;

import com.yezishuo.transfer.dto.*;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.repository.TransferDeleteAuditRepository;
import com.yezishuo.transfer.repository.TransferRecordRepository;
import com.yezishuo.transfer.service.TransferService;
import com.yezishuo.transfer.service.NotificationService;
import com.yezishuo.transfer.service.WeChatWorkNotifyService;
import com.yezishuo.usermanagement.entity.User;
import com.yezishuo.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransferDeleteAuditRepository auditRepository;

    @Autowired
    private TransferRecordRepository recordRepository;

    // 获取当前用户信息
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Object userRole = session.getAttribute("userRole");
        Object userName = session.getAttribute("userName");
        Object userId = session.getAttribute("userId");
        Object isLoggedIn = session.getAttribute("isLoggedIn");

        if (userRole == null) {
            Object roleLevel = session.getAttribute("roleLevel");
            if (roleLevel != null) {
                int level = (int) roleLevel;
                if (level == 0) {
                    userRole = "SUPER";
                } else if (level == 1) {
                    userRole = "NORMAL";
                } else {
                    userRole = "GUEST";
                }
            } else {
                userRole = "GUEST";
            }
        }

        boolean loggedIn = isLoggedIn != null && (boolean) isLoggedIn;
        if (!loggedIn && userId != null) {
            loggedIn = true;
        }

        result.put("isLoggedIn", loggedIn);
        result.put("userRole", userRole);
        result.put("userName", userName != null ? userName : "未知用户");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/records")
    public ResponseEntity<?> getRecords(HttpSession session) {
        List<TransferRecord> records = transferService.getAllRecords();
        Map<String, Object> stats = transferService.getStatistics();

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("statistics", stats);
        result.put("userRole", session.getAttribute("userRole"));
        result.put("userName", session.getAttribute("userName"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/record")
    public ResponseEntity<?> addRecord(@RequestBody TransferRecordDTO dto, HttpSession session) {
        String role = getString(session, "userRole");
        if ("GUEST".equals(role)) {
            return ResponseEntity.status(403).body(errorMap("游客无权新增记录"));
        }

        String operator = getString(session, "userName");
        TransferRecord record = transferService.addRecord(dto, operator);
        return ResponseEntity.ok(record);
    }

    @DeleteMapping("/record/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable String id,
                                          @RequestParam(required = false) String reason,
                                          HttpSession session) {
        String role = getString(session, "userRole");
        String userName = getString(session, "userName");
        Integer userId = (Integer) session.getAttribute("userId");

        if ("GUEST".equals(role)) {
            return ResponseEntity.status(403).body(errorMap("游客无权删除记录"));
        }

        // 检查删除原因是否为空
        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap("请填写删除原因"));
        }

        if ("SUPER".equals(role)) {
            // 超级账号直接删除（自动审核通过）
            transferService.requestDelete(id, userName, userId, reason);

            // 超级账号自动审核通过
            List<TransferDeleteAudit> pending = transferService.getPendingAudits();
            for (TransferDeleteAudit audit : pending) {
                if (audit.getTransferRecordId().equals(id)) {
                    TransferAuditDTO auditDTO = new TransferAuditDTO();
                    auditDTO.setAuditId(audit.getId());
                    auditDTO.setAuditStatus("approved");
                    auditDTO.setAuditUser(userName);
                    auditDTO.setAuditRemark("超级账号直接删除，原因：" + reason);
                    transferService.auditDelete(auditDTO);
                    break;
                }
            }
            return ResponseEntity.ok(successMap("message", "删除成功"));
        } else {
            // 普通账号提交审核
            transferService.requestDelete(id, userName, userId, reason);
            return ResponseEntity.ok(successMap("message", "已提交删除申请，等待超级账号审核"));
        }
    }

    @GetMapping("/pending-audits")
    public ResponseEntity<?> getPendingAudits(HttpSession session) {
        String role = getString(session, "userRole");
        if (!"SUPER".equals(role)) {
            return ResponseEntity.status(403).body(errorMap("无权限查看审核列表"));
        }

        List<TransferDeleteAudit> audits = transferService.getPendingAudits();
        return ResponseEntity.ok(audits);
    }

    @PostMapping("/audit")
    public ResponseEntity<?> auditDelete(@RequestBody TransferAuditDTO auditDTO, HttpSession session) {
        String role = getString(session, "userRole");
        if (!"SUPER".equals(role)) {
            return ResponseEntity.status(403).body(errorMap("只有超级账号可以审核"));
        }

        auditDTO.setAuditUser(getString(session, "userName"));
        transferService.auditDelete(auditDTO);
        return ResponseEntity.ok(successMap("message", "审核完成"));
    }

    // ==================== 通知接口（Java 8 兼容版本） ====================

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(new ArrayList<>());  // Java 8 兼容写法
        }
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("count", 0);
            return ResponseEntity.ok(result);
        }
        long count = notificationService.getUnreadCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "已标记为已读");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<?> markAllRead(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            notificationService.markAllAsRead(userId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("message", "全部已读");
        return ResponseEntity.ok(result);
    }

    // ==================== 辅助方法（Java 8 兼容） ====================

    private String getString(HttpSession session, String key) {
        Object value = session.getAttribute(key);
        return value != null ? value.toString() : "GUEST";
    }

    private Map<String, Object> successMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Map<String, Object> errorMap(String error) {
        Map<String, Object> map = new HashMap<>();
        map.put("error", error);
        return map;
    }

    // 真正退出登录
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();  // 销毁session
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "退出成功");
        return ResponseEntity.ok(result);
    }

    // 获取删除申请详情（供超级管理员审核用）
    @GetMapping("/audit-detail/{auditId}")
    public ResponseEntity<?> getAuditDetail(@PathVariable String auditId) {
        TransferDeleteAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("审核记录不存在"));
        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new RuntimeException("调货记录不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("audit", audit);
        result.put("record", record);
        return ResponseEntity.ok(result);
    }

    // 获取审核结果详情
    @GetMapping("/audit-result-detail/{auditId}")
    public ResponseEntity<?> getAuditResultDetail(@PathVariable String auditId) {
        TransferDeleteAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("审核记录不存在"));
        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new RuntimeException("调货记录不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("audit", audit);
        result.put("record", record);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch-record")
    public ResponseEntity<?> addBatchRecords(@RequestBody BatchTransferDTO batchDTO, HttpSession session) {
        String role = getString(session, "userRole");
        if ("GUEST".equals(role)) {
            return ResponseEntity.status(403).body(errorMap("游客无权新增记录"));
        }

        String operator = getString(session, "userName");
        List<TransferRecord> records = transferService.addBatchRecords(batchDTO, operator);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "新增成功");
        result.put("count", records.size());
        return ResponseEntity.ok(result);
    }
}