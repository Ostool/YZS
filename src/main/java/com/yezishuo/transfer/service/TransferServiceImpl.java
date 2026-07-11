package com.yezishuo.transfer.service;

import com.yezishuo.inventory.service.InventoryLedgerService;
import com.yezishuo.transfer.dto.BatchItemDTO;
import com.yezishuo.transfer.dto.BatchTransferDTO;
import com.yezishuo.transfer.dto.TransferRecordDTO;
import com.yezishuo.transfer.dto.TransferAuditDTO;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.repository.TransferRecordRepository;
import com.yezishuo.transfer.repository.TransferDeleteAuditRepository;
import com.yezishuo.transfer.util.ShopNameUtil;
import com.yezishuo.transfer.exception.BusinessException;
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
    private final InventoryLedgerService ledgerService;

    @Autowired
    public TransferServiceImpl(TransferRecordRepository recordRepository,
                               TransferDeleteAuditRepository auditRepository,
                               WeChatWorkNotifyService notifyService,
                               NotificationService notificationService,
                               InventoryLedgerService ledgerService) {
        this.recordRepository = recordRepository;
        this.auditRepository = auditRepository;
        this.notifyService = notifyService;
        this.notificationService = notificationService;
        this.ledgerService = ledgerService;
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

        TransferRecord record = buildRecord(dto, operator);
        TransferRecord saved = recordRepository.save(record);
        log.info("调货记录保存成功，ID: {}", saved.getId());

        syncInventoryOnCreate(saved);

        sendAddNotification(saved, operator);

        return saved;
    }

    @Override
    @Transactional
    public void requestDelete(String recordId, String requestUser, Integer requestUserId, String reason) {
        TransferRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("记录不存在"));

        TransferDeleteAudit existing = auditRepository.findByTransferRecordIdAndAuditStatus(recordId, "pending");
        if (existing != null) {
            throw new BusinessException("该记录已有待审核的删除请求");
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

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("货品", record.getProductName());
        details.put("数量", record.getQuantity());
        details.put("方向明细", record.getDirectionDetail());
        details.put("取货时间", formatDateTime(record.getTransferTime()));
        details.put("删除原因", reason);
        notifyService.sendDeleteRequestNotification(requestUser, details);

        if (notificationService != null) {
            notificationService.createDeleteRequestNotification(record, audit, requestUser);
        }
    }

    @Override
    public List<TransferDeleteAudit> getPendingAudits() {
        return auditRepository.findByAuditStatusOrderByRequestTimeDesc("pending");
    }

    @Override
    @Transactional
    public void auditDelete(TransferAuditDTO auditDTO) {
        TransferDeleteAudit audit = auditRepository.findById(auditDTO.getAuditId())
                .orElseThrow(() -> new BusinessException("审核记录不存在"));

        boolean approved = "approved".equals(auditDTO.getAuditStatus());

        if (approved && !"approved".equals(audit.getAuditStatus())) {
            audit.setAuditStatus(auditDTO.getAuditStatus());
            audit.setAuditUser(auditDTO.getAuditUser());
            audit.setAuditTime(LocalDateTime.now());
            audit.setAuditRemark(auditDTO.getAuditRemark());
            auditRepository.save(audit);

            TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                    .orElseThrow(() -> new BusinessException("调货记录不存在"));
            record.setStatus("deleted");
            recordRepository.save(record);

            rollbackInventoryOnDelete(record, auditDTO.getAuditUser());
        } else {
            audit.setAuditStatus(auditDTO.getAuditStatus());
            audit.setAuditUser(auditDTO.getAuditUser());
            audit.setAuditTime(LocalDateTime.now());
            audit.setAuditRemark(auditDTO.getAuditRemark());
            auditRepository.save(audit);
        }

        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new BusinessException("调货记录不存在"));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("货品", record.getProductName());
        details.put("数量", record.getQuantity());
        notifyService.sendAuditResultNotification(auditDTO.getAuditUser(), details, approved,
                auditDTO.getAuditRemark(), audit.getRequestUser());

        if (notificationService != null) {
            notificationService.createAuditResultNotification(record, audit, auditDTO.getAuditUser(),
                    approved ? "approved" : "rejected");
        }
    }

    @Override
    @Transactional
    public TransferRecord updateRecord(TransferRecordDTO dto, String operator) {
        TransferRecord record = recordRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException("记录不存在"));

        if (!"active".equals(record.getStatus())) {
            throw new BusinessException("只能修改活跃状态的记录");
        }

        rollbackInventoryOnUpdate(record, operator);

        record.setTransferTime(dto.getTransferTime());
        record.setDirection(dto.getDirection());
        record.setDirectionDetail(dto.getDirectionDetail());
        record.setPickupPerson(dto.getPickupPerson());
        record.setProductType(dto.getProductType());
        record.setBrand(dto.getBrand());
        record.setSeries(dto.getSeries());
        record.setModel(dto.getModel());
        record.setProductName(dto.getProductName());
        record.setQuantity(dto.getQuantity());
        record.setRemark(dto.getRemark());
        record.setUpdateTime(LocalDateTime.now());

        TransferRecord saved = recordRepository.save(record);

        syncInventoryOnCreate(saved);

        log.info("调货记录已更新，操作人: {}, recordId: {}", operator, saved.getId());
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

    @Override
    @Transactional
    public List<TransferRecord> addBatchRecords(BatchTransferDTO batchDTO, String operator) {
        log.info("开始批量新增调货记录，操作人: {}, 货品数量: {}", operator, batchDTO.getItems().size());

        List<TransferRecord> savedRecords = new ArrayList<>();

        for (BatchItemDTO item : batchDTO.getItems()) {
            TransferRecord record = new TransferRecord();
            record.setId(UUID.randomUUID().toString().replace("-", ""));
            record.setTransferTime(batchDTO.getTransferTime());
            record.setDirection(batchDTO.getDirection());
            record.setDirectionDetail(batchDTO.getDirectionDetail());
            record.setPickupPerson(batchDTO.getPickupPerson());
            record.setProductType(item.getProductType());
            record.setBrand(item.getBrand());
            record.setSeries(item.getSeries());
            record.setModel(item.getModel());
            record.setProductName(item.getProductName());
            record.setQuantity(item.getQuantity());
            record.setRemark(batchDTO.getRemark());
            record.setFiller(batchDTO.getFiller());
            record.setStatus("active");
            record.setCreateTime(LocalDateTime.now());
            record.setCreateBy(operator);
            TransferRecord saved = recordRepository.save(record);
            savedRecords.add(saved);

            syncInventoryOnCreate(saved);
        }

        log.info("批量保存成功，共 {} 条记录", savedRecords.size());

        Map<String, Object> details = new LinkedHashMap<>();
        StringBuilder productList = new StringBuilder();
        for (BatchItemDTO item : batchDTO.getItems()) {
            String typeLabel = "MATERIAL".equals(item.getProductType()) ? "物料" : ("FRAME".equals(item.getProductType()) ? "镜架" : "镜片");
            productList.append("[").append(typeLabel).append("] ");
            if (item.getBrand() != null) productList.append(item.getBrand()).append(" ");
            if (item.getSeries() != null) productList.append(item.getSeries()).append(" ");
            if (item.getModel() != null && !item.getModel().isEmpty()) productList.append(item.getModel()).append(" ");
            if ("MATERIAL".equals(item.getProductType())) productList.append(item.getProductName());
            productList.append("\n");
            productList.append("数量：").append(item.getQuantity()).append("\n\n");
        }
        details.put("货品清单", productList.toString().trim());
        details.put("方向", batchDTO.getDirectionDetail());
        details.put("取货人", batchDTO.getPickupPerson());
        details.put("填写人", batchDTO.getFiller());
        if (batchDTO.getRemark() != null && !batchDTO.getRemark().isEmpty()) {
            details.put("备注", batchDTO.getRemark());
        }

        notifyService.sendBatchAddNotification(operator, details, batchDTO.getItems().size());

        return savedRecords;
    }

    private TransferRecord buildRecord(TransferRecordDTO dto, String operator) {
        TransferRecord record = new TransferRecord();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setTransferTime(dto.getTransferTime());
        record.setDirection(dto.getDirection());
        record.setDirectionDetail(dto.getDirectionDetail());
        record.setPickupPerson(dto.getPickupPerson());
        record.setProductType(dto.getProductType());
        record.setBrand(dto.getBrand());
        record.setSeries(dto.getSeries());
        record.setModel(dto.getModel());
        record.setProductName(dto.getProductName());
        record.setQuantity(dto.getQuantity());
        record.setRemark(dto.getRemark());
        record.setFiller(dto.getFiller());
        record.setStatus("active");
        record.setCreateTime(LocalDateTime.now());
        record.setCreateBy(operator);
        return record;
    }

    private void syncInventoryOnCreate(TransferRecord record) {
        if (record.getProductType() == null) return;
        if (!"FRAME".equals(record.getProductType()) && !"LENS".equals(record.getProductType())) return;

        try {
            String[] parsed = ShopNameUtil.parseDirectionDetail(record.getDirectionDetail());
            if (parsed == null) {
                log.debug("无法解析方向明细，跳过库存同步: {}", record.getDirectionDetail());
                return;
            }
            String destShop = ShopNameUtil.resolveShopName(parsed[0]);
            String sourceShop = ShopNameUtil.resolveShopName(parsed[1]);
            String productType = record.getProductType();
            String brand = record.getBrand();
            String series = record.getSeries();
            String model = record.getModel();
            int quantity = record.getQuantity();

            if (brand == null || series == null) {
                log.warn("调货记录品牌或系列为空，跳过库存同步: id={}", record.getId());
                return;
            }

            ledgerService.recordTransferOut(sourceShop, productType, brand, series, model,
                    quantity, record.getId(), record.getFiller(),
                    "调出至 " + destShop + "（调货记录）");

            ledgerService.recordTransferIn(destShop, productType, brand, series, model,
                    quantity, record.getId(), record.getFiller(),
                    "从 " + sourceShop + " 调入（调货记录）");
        } catch (Exception e) {
            log.error("同步库存失败，调货记录ID: {}, error: {}", record.getId(), e.getMessage(), e);
        }
    }

    private void rollbackInventoryOnUpdate(TransferRecord oldRecord, String operator) {
        if (oldRecord.getProductType() == null) return;
        if (!"FRAME".equals(oldRecord.getProductType()) && !"LENS".equals(oldRecord.getProductType())) return;

        try {
            String[] parsed = ShopNameUtil.parseDirectionDetail(oldRecord.getDirectionDetail());
            if (parsed == null) return;

            String destShop = ShopNameUtil.resolveShopName(parsed[0]);
            String sourceShop = ShopNameUtil.resolveShopName(parsed[1]);

            if (oldRecord.getBrand() == null || oldRecord.getSeries() == null) return;

            int quantity = oldRecord.getQuantity();
            String reason = "更新调货记录前回滚: " + oldRecord.getId();

            ledgerService.recordTransferRollbackIn(sourceShop, oldRecord.getProductType(),
                    oldRecord.getBrand(), oldRecord.getSeries(), oldRecord.getModel(),
                    quantity, oldRecord.getId(), operator, reason);

            ledgerService.recordTransferRollbackOut(destShop, oldRecord.getProductType(),
                    oldRecord.getBrand(), oldRecord.getSeries(), oldRecord.getModel(),
                    quantity, oldRecord.getId(), operator, reason);

            log.info("更新调货回滚库存完成: recordId={}", oldRecord.getId());
        } catch (Exception e) {
            log.error("更新调货回滚库存失败, recordId={}, error: {}", oldRecord.getId(), e.getMessage(), e);
        }
    }

    private void rollbackInventoryOnDelete(TransferRecord record, String operator) {
        if (record.getProductType() == null) return;
        if (!"FRAME".equals(record.getProductType()) && !"LENS".equals(record.getProductType())) return;

        try {
            String[] parsed = ShopNameUtil.parseDirectionDetail(record.getDirectionDetail());
            if (parsed == null) return;

            String destShop = ShopNameUtil.resolveShopName(parsed[0]);
            String sourceShop = ShopNameUtil.resolveShopName(parsed[1]);

            if (record.getBrand() == null || record.getSeries() == null) return;

            int quantity = record.getQuantity();
            String reason = "删除调货记录: " + record.getId();

            ledgerService.recordTransferRollbackIn(sourceShop, record.getProductType(),
                    record.getBrand(), record.getSeries(), record.getModel(),
                    quantity, record.getId(), operator, "删除回滚-源店恢复");

            ledgerService.recordTransferRollbackOut(destShop, record.getProductType(),
                    record.getBrand(), record.getSeries(), record.getModel(),
                    quantity, record.getId(), operator, "删除回滚-目标店扣回");

            log.info("删除调货回滚库存完成: recordId={}", record.getId());
        } catch (Exception e) {
            log.error("删除调货回滚库存失败, recordId={}, error: {}", record.getId(), e.getMessage(), e);
        }
    }

    private void sendAddNotification(TransferRecord saved, String operator) {
        Map<String, Object> details = new LinkedHashMap<>();
        String typeLabel = "MATERIAL".equals(saved.getProductType()) ? "物料" : ("FRAME".equals(saved.getProductType()) ? "镜架" : "镜片");
        details.put("类型", typeLabel);
        if (saved.getBrand() != null) details.put("品牌", saved.getBrand());
        if (saved.getSeries() != null) details.put("系列", saved.getSeries());
        if (saved.getModel() != null && !saved.getModel().isEmpty()) details.put("型号", saved.getModel());
        if ("MATERIAL".equals(saved.getProductType())) {
            details.put("货品", saved.getProductName());
        }
        details.put("数量", saved.getQuantity());
        details.put("方向", saved.getDirectionDetail());
        details.put("取货人", saved.getPickupPerson());
        details.put("填写人", saved.getFiller());
        if (saved.getRemark() != null && !saved.getRemark().isEmpty()) {
            details.put("备注", saved.getRemark());
        }
        notifyService.sendAddNotification(operator, details);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
