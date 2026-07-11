package com.yezishuo.transfer.controller;

import com.yezishuo.transfer.dto.*;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.util.ShopNameUtil;
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
import java.util.stream.Collectors;

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
        List<TransferRecord> allRecords = transferService.getAllRecords();

        if (!SecurityUtils.isSuperAdmin()) {
            String myShop = getEffectiveShopName(session);
            if (myShop != null && !myShop.isEmpty()) {
                String shortName = ShopNameUtil.toShortName(myShop);
                if (shortName != null) {
                    allRecords = allRecords.stream()
                            .filter(r -> r.getDirectionDetail() != null && r.getDirectionDetail().contains(shortName))
                            .collect(Collectors.toList());
                }
            }
        }

        Map<String, Object> stats = transferService.getStatistics();
        // Recompute stats for filtered records if not super admin
        if (!SecurityUtils.isSuperAdmin()) {
            int totalOut = allRecords.stream().filter(r -> "out".equals(r.getDirection())).mapToInt(TransferRecord::getQuantity).sum();
            int totalIn = allRecords.stream().filter(r -> "in".equals(r.getDirection())).mapToInt(TransferRecord::getQuantity).sum();
            stats.put("totalOut", totalOut);
            stats.put("totalIn", totalIn);
            stats.put("netOutflow", totalOut - totalIn);
            stats.put("totalCount", allRecords.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", allRecords);
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

        if (!SecurityUtils.isSuperAdmin()) {
            TransferRecord record = recordRepository.findById(id).orElse(null);
            if (record == null) {
                return ResponseEntity.badRequest().body(errorMap("记录不存在"));
            }
            String myShop = getEffectiveShopName(session);
            String shortName = ShopNameUtil.toShortName(myShop);
            if (shortName == null || record.getDirectionDetail() == null
                    || !record.getDirectionDetail().contains(shortName)) {
                return ResponseEntity.status(403).body(errorMap("无权删除其他门店的记录"));
            }
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

    @GetMapping("/admin/cleanup-before-july2026")
    public ResponseEntity<?> cleanupOldData() {
        if (!SecurityUtils.isSuperAdmin()) {
            return ResponseEntity.status(403).body(errorMap("仅超级账号可执行"));
        }
        try {
            java.time.LocalDateTime cutoff = java.time.LocalDateTime.of(2026, 7, 1, 0, 0);
            List<TransferRecord> all = recordRepository.findAllActiveOrderByCreateTimeDesc();
            int deleted = 0;
            for (TransferRecord r : all) {
                if (r.getCreateTime() != null && r.getCreateTime().isBefore(cutoff)) {
                    r.setStatus("deleted");
                    recordRepository.save(r);
                    deleted++;
                }
            }
            List<TransferDeleteAudit> audits = auditRepository.findAll();
            for (TransferDeleteAudit a : audits) {
                if (a.getRequestTime() != null && a.getRequestTime().isBefore(cutoff)) {
                    auditRepository.delete(a);
                }
            }
            return ResponseEntity.ok(successMap("message", "已清理 " + deleted + " 条旧记录，仅保留2026年7月及之后数据"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(errorMap("清理失败: " + e.getMessage()));
        }
    }

    // ==================== 辅助方法 ====================

    private String getEffectiveShopName(HttpSession session) {
        UserDetailsImpl user = SecurityUtils.getCurrentUser();
        if (user != null) {
            String shopName = user.getShopName();
            if (shopName != null && !shopName.isEmpty()) return shopName;
        }
        Object shopName = session.getAttribute("shopName");
        if (shopName != null) return shopName.toString();
        Object userName = session.getAttribute("userName");
        if (userName != null) return userName.toString();
        return null;
    }

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
