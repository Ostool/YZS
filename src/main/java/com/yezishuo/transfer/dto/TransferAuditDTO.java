// TransferAuditDTO.java
package com.yezishuo.transfer.dto;

import lombok.Data;

@Data
public class TransferAuditDTO {
    private String auditId;
    private String auditStatus; // approved/rejected
    private String auditUser;
    private String auditRemark;
}