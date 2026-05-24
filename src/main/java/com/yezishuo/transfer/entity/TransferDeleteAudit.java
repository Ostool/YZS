package com.yezishuo.transfer.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_delete_audit")
public class TransferDeleteAudit {

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "transfer_record_id", nullable = false, length = 32)
    private String transferRecordId;

    @Column(name = "request_user", nullable = false, length = 50)
    private String requestUser;

    @Column(name = "request_user_id")
    private Integer requestUserId;

    @Column(name = "delete_reason", length = 500)  // 新增删除原因字段
    private String deleteReason;

    @Column(name = "request_time")
    private LocalDateTime requestTime;

    @Column(name = "audit_status", length = 20)
    private String auditStatus = "pending";

    @Column(name = "audit_user", length = 50)
    private String auditUser;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "audit_remark", length = 200)
    private String auditRemark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTransferRecordId() { return transferRecordId; }
    public void setTransferRecordId(String transferRecordId) { this.transferRecordId = transferRecordId; }

    public String getRequestUser() { return requestUser; }
    public void setRequestUser(String requestUser) { this.requestUser = requestUser; }

    public Integer getRequestUserId() { return requestUserId; }
    public void setRequestUserId(Integer requestUserId) { this.requestUserId = requestUserId; }

    public String getDeleteReason() { return deleteReason; }  // 新增
    public void setDeleteReason(String deleteReason) { this.deleteReason = deleteReason; }  // 新增

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditUser() { return auditUser; }
    public void setAuditUser(String auditUser) { this.auditUser = auditUser; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}