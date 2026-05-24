package com.yezishuo.transfer.service;

import com.yezishuo.transfer.dto.TransferRecordDTO;
import com.yezishuo.transfer.dto.TransferAuditDTO;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.repository.TransferRecordRepository;
import com.yezishuo.transfer.repository.TransferDeleteAuditRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final TransferRecordRepository recordRepository;
    private final TransferDeleteAuditRepository auditRepository;
    private final WeChatWorkNotifyService notifyService;
    private final NotificationService notificationService;

    @Autowired
    public TransferServiceImpl(TransferRecordRepository recordRepository,
                               TransferDeleteAuditRepository auditRepository,
                               WeChatWorkNotifyService notifyService,
                               NotificationService notificationService) {
        this.recordRepository = recordRepository;
        this.auditRepository = auditRepository;
        this.notifyService = notifyService;
        this.notificationService = notificationService;
    }

    @Override
    public List<TransferRecord> getAllRecords() {
        log.info("获取所有调货记录，按创建时间倒序");
        List<TransferRecord> records = recordRepository.findAllActiveOrderByCreateTimeDesc();
        log.info("共获取到 {} 条记录", records.size());
        return records;
    }

    @Override
    @Transactional
    public TransferRecord addRecord(TransferRecordDTO dto, String operator) {
        log.info("开始新增调货记录，操作人: {}", operator);

        TransferRecord record = new TransferRecord();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setTransferTime(dto.getTransferTime());
        record.setDirection(dto.getDirection());
        record.setDirectionDetail(dto.getDirectionDetail());
        record.setPickupPerson(dto.getPickupPerson());
        record.setProductName(dto.getProductName());
        record.setQuantity(dto.getQuantity());
        record.setRemark(dto.getRemark());
        record.setFiller(dto.getFiller());
        record.setStatus("active");
        record.setCreateTime(LocalDateTime.now());
        record.setCreateBy(operator);

        TransferRecord saved = recordRepository.save(record);
        log.info("调货记录保存成功，ID: {}", saved.getId());

        // 发送新增通知
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("货品", record.getProductName());
        details.put("数量", record.getQuantity());
        details.put("方向", record.getDirectionDetail());
        details.put("取货人", record.getPickupPerson());
        details.put("填写人", record.getFiller());
        if (record.getRemark() != null && !record.getRemark().isEmpty()) {
            details.put("备注", record.getRemark());
        }
        notifyService.sendAddNotification(operator, details);

        return saved;
    }

    @Override
    @Transactional
    public void requestDelete(String recordId, String requestUser, Integer requestUserId, String reason) {
        TransferRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 检查是否已有待审核的删除请求
        TransferDeleteAudit existing = auditRepository.findByTransferRecordIdAndAuditStatus(recordId, "pending");
        if (existing != null) {
            throw new RuntimeException("该记录已有待审核的删除请求");
        }

        TransferDeleteAudit audit = new TransferDeleteAudit();
        audit.setId(UUID.randomUUID().toString().replace("-", ""));
        audit.setTransferRecordId(recordId);
        audit.setRequestUser(requestUser);
        audit.setRequestUserId(requestUserId);
        audit.setDeleteReason(reason);
        audit.setRequestTime(LocalDateTime.now());
        audit.setAuditStatus("pending");
        audit.setCreateTime(LocalDateTime.now());
        auditRepository.save(audit);

        // 发送删除请求通知（使用新方法）
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("货品", record.getProductName());
        details.put("数量", record.getQuantity());
        details.put("方向明细", record.getDirectionDetail());
        details.put("取货时间", formatDateTime(record.getTransferTime()));
        details.put("删除原因", reason);
        notifyService.sendDeleteRequestNotification(requestUser, details);

        // 创建通知给超级管理员
        if (notificationService != null) {
            notificationService.createDeleteRequestNotification(record, audit, requestUser);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    @Override
    public List<TransferDeleteAudit> getPendingAudits() {
        return auditRepository.findByAuditStatusOrderByRequestTimeDesc("pending");
    }

    @Override
    @Transactional
    public void auditDelete(TransferAuditDTO auditDTO) {
        TransferDeleteAudit audit = auditRepository.findById(auditDTO.getAuditId())
                .orElseThrow(() -> new RuntimeException("审核记录不存在"));

        audit.setAuditStatus(auditDTO.getAuditStatus());
        audit.setAuditUser(auditDTO.getAuditUser());
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditRemark(auditDTO.getAuditRemark());
        auditRepository.save(audit);

        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new RuntimeException("调货记录不存在"));

        boolean approved = "approved".equals(auditDTO.getAuditStatus());

        if (approved) {
            record.setStatus("deleted");
            recordRepository.save(record);
        }

        // 发送审核结果通知
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("货品", record.getProductName());
        details.put("数量", record.getQuantity());
        notifyService.sendAuditResultNotification(auditDTO.getAuditUser(), details, approved,
                auditDTO.getAuditRemark(), audit.getRequestUser());

        // 创建通知给申请人
        if (notificationService != null) {
            notificationService.createAuditResultNotification(record, audit, auditDTO.getAuditUser(),
                    approved ? "approved" : "rejected");
        }
    }

    @Override
    @Transactional
    public TransferRecord updateRecord(TransferRecordDTO dto, String operator) {
        TransferRecord record = recordRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 记录变更前的信息
        String oldProduct = record.getProductName();
        Integer oldQuantity = record.getQuantity();

        record.setTransferTime(dto.getTransferTime());
        record.setDirection(dto.getDirection());
        record.setDirectionDetail(dto.getDirectionDetail());
        record.setPickupPerson(dto.getPickupPerson());
        record.setProductName(dto.getProductName());
        record.setQuantity(dto.getQuantity());
        record.setRemark(dto.getRemark());
        record.setUpdateTime(LocalDateTime.now());

        TransferRecord saved = recordRepository.save(record);

        // 发送更新通知（如果需要，可以保持原有方式或删除）
        // 注意：如果不需要更新通知，可以注释掉
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("变更前货品", oldProduct);
        details.put("变更后货品", record.getProductName());
        details.put("变更前数量", oldQuantity);
        details.put("变更后数量", record.getQuantity());
        // 如果 WeChatWorkNotifyService 有更新通知方法，可以调用
        // notifyService.sendUpdateNotification(operator, details);
        log.info("调货记录已更新，操作人: {}", operator);

        return saved;
    }

    @Override
    public Map<String, Object> getStatistics() {
        List<TransferRecord> records = getAllRecords();
        int totalOut = records.stream().filter(r -> "out".equals(r.getDirection())).mapToInt(TransferRecord::getQuantity).sum();
        int totalIn = records.stream().filter(r -> "in".equals(r.getDirection())).mapToInt(TransferRecord::getQuantity).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOut", totalOut);
        stats.put("totalIn", totalIn);
        stats.put("netOutflow", totalOut - totalIn);
        stats.put("totalCount", records.size());
        return stats;
    }
}