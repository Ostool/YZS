package com.yezishuo.transfer.controller;

import com.yezishuo.transfer.dto.*;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.exception.BusinessException;
import com.yezishuo.transfer.repository.TransferDeleteAuditRepository;
import com.yezishuo.transfer.repository.TransferRecordRepository;
import com.yezishuo.transfer.service.TransferService;
import com.yezishuo.transfer.service.NotificationService;
import com.yezishuo.usermanagement.security.SecurityUtils;
import com.yezishuo.usermanagement.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    @Autowired
    private TransferService transferService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransferDeleteAuditRepository auditRepository;

    @Autowired
    private TransferRecordRepository recordRepository;

    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        Object userRole = session.getAttribute("userRole");
        Object userName = session.getAttribute("userName");

        if (userRole == null) {
            UserDetailsImpl user = SecurityUtils.getCurrentUser();
            if (user != null) {
                userRole = user.getTransferRole();
                userName = getDisplayName(user);
            } else {
                userRole = "GUEST";
                userName = "未知用户";
            }
        }

        boolean loggedIn = session.getAttribute("userId") != null
                || SecurityUtils.getCurrentUserId() != null;

        result.put("isLoggedIn", loggedIn);
        result.put("userRole", userRole);
        result.put("userName", userName != null ? userName : "未知用户");
        result.put("realName", session.getAttribute("realName"));

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
        result.put("realName", session.getAttribute("realName"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/record")
    public ResponseEntity<?> addRecord(@RequestBody TransferRecordDTO dto, HttpSession session) {
        if (SecurityUtils.isGuest()) {
            return ResponseEntity.status(403).body(errorMap("游客无权新增记录"));
        }
        String operator = getUserName(session);
        TransferRecord record = transferService.addRecord(dto, operator);
        return ResponseEntity.ok(record);
    }

    @DeleteMapping("/record/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable String id,
                                          @RequestParam(required = false) String reason,
                                          HttpSession session) {
        if (SecurityUtils.isGuest()) {
            return ResponseEntity.status(403).body(errorMap("游客无权删除记录"));
        }

        if (reason == null || reason.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(errorMap("请填写删除原因"));
        }

        String userName = getUserName(session);
        Integer userId = SecurityUtils.getCurrentUserId();

        if (SecurityUtils.isSuperAdmin()) {
            transferService.requestDelete(id, userName, userId, reason);
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
            transferService.requestDelete(id, userName, userId, reason);
            return ResponseEntity.ok(successMap("message", "已提交删除申请，等待超级账号审核"));
        }
    }

    @GetMapping("/pending-audits")
    public ResponseEntity<?> getPendingAudits() {
        if (!SecurityUtils.isSuperAdmin()) {
            return ResponseEntity.status(403).body(errorMap("无权限查看审核列表"));
        }
        List<TransferDeleteAudit> audits = transferService.getPendingAudits();
        return ResponseEntity.ok(audits);
    }

    @PostMapping("/audit")
    public ResponseEntity<?> auditDelete(@RequestBody TransferAuditDTO auditDTO, HttpSession session) {
        if (!SecurityUtils.isSuperAdmin()) {
            return ResponseEntity.status(403).body(errorMap("只有超级账号可以审核"));
        }
        auditDTO.setAuditUser(getUserName(session));
        transferService.auditDelete(auditDTO);
        return ResponseEntity.ok(successMap("message", "审核完成"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        Integer userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        if (userId == null) {
            result.put("count", 0);
            return ResponseEntity.ok(result);
        }
        long count = notificationService.getUnreadCount(userId);
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
    public ResponseEntity<?> markAllRead() {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            notificationService.markAllAsRead(userId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("message", "全部已读");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "退出成功");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/audit-detail/{auditId}")
    public ResponseEntity<?> getAuditDetail(@PathVariable String auditId) {
        TransferDeleteAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new BusinessException("审核记录不存在"));
        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new BusinessException("调货记录不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("audit", audit);
        result.put("record", record);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/audit-result-detail/{auditId}")
    public ResponseEntity<?> getAuditResultDetail(@PathVariable String auditId) {
        TransferDeleteAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new BusinessException("审核记录不存在"));
        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new BusinessException("调货记录不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("audit", audit);
        result.put("record", record);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch-record")
    public ResponseEntity<?> addBatchRecords(@RequestBody BatchTransferDTO batchDTO, HttpSession session) {
        if (SecurityUtils.isGuest()) {
            return ResponseEntity.status(403).body(errorMap("游客无权新增记录"));
        }
        String operator = getUserName(session);
        List<TransferRecord> records = transferService.addBatchRecords(batchDTO, operator);
        return ResponseEntity.ok(records);
    }

    // ==================== 辅助方法 ====================

    private String getUserName(HttpSession session) {
        UserDetailsImpl user = SecurityUtils.getCurrentUser();
        if (user != null) {
            return getDisplayName(user);
        }
        Object name = session.getAttribute("userName");
        return name != null ? name.toString() : "GUEST";
    }

    private String getDisplayName(UserDetailsImpl user) {
        String displayName = user.getShopName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getRealName();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getUsername();
        }
        return displayName;
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
}
