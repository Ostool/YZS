package com.yezishuo.transfer.service;

import com.yezishuo.transfer.dto.NotificationDTO;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.entity.TransferNotification;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.repository.TransferNotificationRepository;
import com.yezishuo.usermanagement.entity.User;
import com.yezishuo.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NotificationService {

    @Autowired
    private TransferNotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建删除请求通知（发送给超级管理员）
     */
    @Transactional
    public void createDeleteRequestNotification(TransferRecord record, TransferDeleteAudit audit, String requesterName) {
        // 查找所有超级管理员用户 (roleLevel = 0)
        List<User> superAdmins = userRepository.findByRoleLevel(0);

        for (User admin : superAdmins) {
            TransferNotification notification = new TransferNotification();
            notification.setId(UUID.randomUUID().toString().replace("-", ""));
            notification.setUserId(admin.getId());
            notification.setUserRole("SUPER");
            notification.setTitle("🗑️ 删除申请待审核");
            notification.setContent(String.format("用户【%s】申请删除调货记录：%s (数量: %d)，请及时处理。",
                    requesterName, record.getProductName(), record.getQuantity()));
            notification.setType("delete_request");
            notification.setAuditId(audit.getId());
            notification.setRecordId(record.getId());
            notification.setIsRead(false);
            notification.setCreateTime(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * 创建审核结果通知（发送给申请删除的用户）
     */
    @Transactional
    public void createAuditResultNotification(TransferRecord record, TransferDeleteAudit audit, String auditorName, String result) {
        String title = result.equals("approved") ? "✅ 删除申请已通过" : "❌ 删除申请已被拒绝";
        String content = result.equals("approved")
                ? String.format("您申请的删除记录【%s】已被【%s】审核通过，记录已删除。", record.getProductName(), auditorName)
                : String.format("您申请的删除记录【%s】已被【%s】拒绝，请查看详情。", record.getProductName(), auditorName);

        TransferNotification notification = new TransferNotification();
        notification.setId(UUID.randomUUID().toString().replace("-", ""));
        notification.setUserId(audit.getRequestUserId());
        notification.setUserRole("NORMAL");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType("audit_result");
        notification.setAuditId(audit.getId());
        notification.setRecordId(record.getId());
        notification.setIsRead(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * 获取用户的通知列表
     */
    public List<NotificationDTO> getUserNotifications(Integer userId) {
        List<TransferNotification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<NotificationDTO> result = new ArrayList<>();

        for (TransferNotification n : notifications) {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(n.getId());
            dto.setTitle(n.getTitle());
            dto.setContent(n.getContent());
            dto.setType(n.getType());
            dto.setAuditId(n.getAuditId());
            dto.setRecordId(n.getRecordId());
            dto.setRead(n.getIsRead());
            dto.setCreateTime(n.getCreateTime() != null ? n.getCreateTime().format(formatter) : "");
            result.add(dto);
        }
        return result;
    }

    /**
     * 获取用户未读通知数量
     */
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * 标记用户所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}