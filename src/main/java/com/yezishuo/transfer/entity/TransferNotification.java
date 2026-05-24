package com.yezishuo.transfer.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_notification")
public class TransferNotification {
    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;  // 接收通知的用户ID

    @Column(name = "user_role", length = 20)
    private String userRole;  // 用户角色

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "type", length = 30)
    private String type;  // delete_request, audit_result

    @Column(name = "audit_id", length = 32)
    private String auditId;  // 关联的审核ID

    @Column(name = "record_id", length = 32)
    private String recordId;  // 关联的记录ID

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}