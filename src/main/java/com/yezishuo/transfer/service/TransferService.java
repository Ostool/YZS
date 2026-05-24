package com.yezishuo.transfer.service;

import com.yezishuo.transfer.dto.TransferRecordDTO;
import com.yezishuo.transfer.dto.TransferAuditDTO;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import java.util.List;
import java.util.Map;

public interface TransferService {
    List<TransferRecord> getAllRecords();
    TransferRecord addRecord(TransferRecordDTO dto, String operator);
    void requestDelete(String recordId, String requestUser, Integer requestUserId, String reason);  // 添加 reason 参数
    List<TransferDeleteAudit> getPendingAudits();
    void auditDelete(TransferAuditDTO auditDTO);
    TransferRecord updateRecord(TransferRecordDTO dto, String operator);
    Map<String, Object> getStatistics();
}