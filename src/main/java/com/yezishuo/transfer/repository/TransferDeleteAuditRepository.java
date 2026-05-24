// TransferDeleteAuditRepository.java
package com.yezishuo.transfer.repository;

import com.yezishuo.transfer.entity.TransferDeleteAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferDeleteAuditRepository extends JpaRepository<TransferDeleteAudit, String> {
    List<TransferDeleteAudit> findByAuditStatusOrderByRequestTimeDesc(String auditStatus);
    TransferDeleteAudit findByTransferRecordIdAndAuditStatus(String transferRecordId, String auditStatus);
}