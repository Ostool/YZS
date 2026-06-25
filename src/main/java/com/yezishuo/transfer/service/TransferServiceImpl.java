package com.yezishuo.transfer.service;

import com.yezishuo.inventory.repository.InventoryItemRepository;
import com.yezishuo.inventory.entity.InventoryItem;
import com.yezishuo.transfer.dto.BatchItemDTO;
import com.yezishuo.transfer.dto.BatchTransferDTO;
import com.yezishuo.transfer.dto.TransferRecordDTO;
import com.yezishuo.transfer.dto.TransferAuditDTO;
import com.yezishuo.transfer.entity.TransferRecord;
import com.yezishuo.transfer.entity.TransferDeleteAudit;
import com.yezishuo.transfer.repository.TransferRecordRepository;
import com.yezishuo.transfer.repository.TransferDeleteAuditRepository;
import com.yezishuo.transfer.util.ShopNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yezishuo.transfer.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    /**
     * 门店短名 → 完整店名映射，由 ShopNameUtil 统一管理
     */
    private static final Map<String, String> SHOP_NAME_MAP = new HashMap<>();
    static {
        SHOP_NAME_MAP.put("1店", "普宁明华体育馆店");
        SHOP_NAME_MAP.put("2店", "普宁广场店");
        SHOP_NAME_MAP.put("3店", "普宁国际商品城店");
        SHOP_NAME_MAP.put("4店", "普宁中华新城店");
        SHOP_NAME_MAP.put("5店", "普宁开心广场店");
        SHOP_NAME_MAP.put("6店", "普宁万泰新天地店");
        SHOP_NAME_MAP.put("进贤门店", "揭阳进贤门店");
        SHOP_NAME_MAP.put("东山店", "揭阳东山店");
        SHOP_NAME_MAP.put("中华路店", "潮阳中华路店");
        SHOP_NAME_MAP.put("谷饶店", "潮阳谷饶店");
        SHOP_NAME_MAP.put("峡山店", "潮南广祥路店");
        SHOP_NAME_MAP.put("两英店", "潮南两英店");
        SHOP_NAME_MAP.put("大坝店", "普宁大坝店");
        SHOP_NAME_MAP.put("总部", "叶子说-总部");
    }

    private final TransferRecordRepository recordRepository;
    private final TransferDeleteAuditRepository auditRepository;
    private final WeChatWorkNotifyService notifyService;
    private final NotificationService notificationService;
    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public TransferServiceImpl(TransferRecordRepository recordRepository,
                               TransferDeleteAuditRepository auditRepository,
                               WeChatWorkNotifyService notifyService,
                               NotificationService notificationService,
                               InventoryItemRepository inventoryItemRepository) {
        this.recordRepository = recordRepository;
        this.auditRepository = auditRepository;
        this.notifyService = notifyService;
        this.notificationService = notificationService;
        this.inventoryItemRepository = inventoryItemRepository;
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

        TransferRecord saved = recordRepository.save(record);
        log.info("调货记录保存成功，ID: {}", saved.getId());

        // 同步货盘库存
        syncInventoryOnTransfer(saved);

        // 发送新增通知
        Map<String, Object> details = new LinkedHashMap<>();
        String typeLabel = "MATERIAL".equals(saved.getProductType()) ? "物料" : ("FRAME".equals(saved.getProductType()) ? "镜架" : "镜片");
        details.put("类型", typeLabel);
        if (saved.getBrand() != null) details.put("品牌", saved.getBrand());
        if (saved.getSeries() != null) details.put("系列", saved.getSeries());
        if (saved.getModel() != null && !saved.getModel().isEmpty()) details.put("型号", saved.getModel());
        // 物料类型用productName，镜架镜片已有品牌系列不重复
        if ("MATERIAL".equals(saved.getProductType())) {
            details.put("货品", saved.getProductName());
        }
        details.put("数量", saved.getQuantity());
        details.put("方向", saved.getDirectionDetail());
        details.put("取货人", saved.getPickupPerson());
        details.put("填写人", saved.getFiller());
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
                .orElseThrow(() -> new BusinessException("记录不存在"));

        // 检查是否已有待审核的删除请求
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
                .orElseThrow(() -> new BusinessException("审核记录不存在"));

        audit.setAuditStatus(auditDTO.getAuditStatus());
        audit.setAuditUser(auditDTO.getAuditUser());
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditRemark(auditDTO.getAuditRemark());
        auditRepository.save(audit);

        TransferRecord record = recordRepository.findById(audit.getTransferRecordId())
                .orElseThrow(() -> new BusinessException("调货记录不存在"));

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
                .orElseThrow(() -> new BusinessException("记录不存在"));

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

    @Override
    @Transactional
    public List<TransferRecord> addBatchRecords(BatchTransferDTO batchDTO, String operator) {
        log.info("开始批量新增调货记录，操作人: {}, 货品数量: {}", operator, batchDTO.getItems().size());

        List<TransferRecord> savedRecords = new ArrayList<>();

        // 逐条保存
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

            // 同步货盘库存
            syncInventoryOnTransfer(saved);
        }

        log.info("批量保存成功，共 {} 条记录", savedRecords.size());

        // ========== 构建合并后的通知内容 ==========
        Map<String, Object> details = new LinkedHashMap<>();

        // 构建货品列表字符串
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

        // 打印日志确认通知内容
        log.info("准备发送企微通知，详情: {}", details);

        // 只发送一条通知
        notifyService.sendBatchAddNotification(operator, details, batchDTO.getItems().size());

        return savedRecords;
    }

    /**
     * 解析方向明细，格式: "X从Y取货"，返回 [destination=X, source=Y]
     */
    private String[] parseDirectionDetail(String directionDetail) {
        if (directionDetail == null || !directionDetail.contains("从") || !directionDetail.contains("取货")) {
            return null;
        }
        int congIdx = directionDetail.indexOf("从");
        int quhuoIdx = directionDetail.indexOf("取货");
        if (congIdx < 0 || quhuoIdx < 0) return null;
        String dest = directionDetail.substring(0, congIdx);
        String source = directionDetail.substring(congIdx + 1, quhuoIdx);
        return new String[]{dest, source};
    }

    /**
     * 将短店名转换为完整店名，若无映射则原样返回
     */
    private String resolveShopName(String name) {
        if (name == null) return null;
        return SHOP_NAME_MAP.getOrDefault(name, name);
    }

    /**
     * 调货时同步货盘库存变动
     */
    private void syncInventoryOnTransfer(TransferRecord record) {
        // 仅镜架类型同步库存
        if (!"FRAME".equals(record.getProductType())) {
            log.debug("非镜架类型调货，跳过库存同步: type={}, name={}", record.getProductType(), record.getProductName());
            return;
        }
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
            Integer quantity = record.getQuantity();

            LocalDateTime now = LocalDateTime.now();

            // 源门店减少库存（优先精确匹配，其次按品牌系列匹配）
            InventoryItem sourceItem = findExactInventoryItem(sourceShop, productType, brand, series, model);
            if (sourceItem == null) {
                sourceItem = findInventoryItemByBrandSeries(sourceShop, productType, brand, series);
            }
            if (sourceItem != null) {
                int newQty = Math.max(0, sourceItem.getQuantity() - quantity);
                sourceItem.setQuantity(newQty);
                sourceItem.setUpdateTime(now);
                inventoryItemRepository.save(sourceItem);
                log.info("调货扣减库存: 门店={}, 货品={} {}(型号={}), 扣减={}, 剩余={}",
                        sourceShop, brand, series, model, quantity, newQty);
            } else {
                log.debug("源门店未找到匹配库存: shop={}, type={}, brand={}, series={}", sourceShop, productType, brand, series);
            }

            // 目标门店增加库存（优先精确匹配，其次按品牌系列匹配，否则创建）
            InventoryItem destItem = findExactInventoryItem(destShop, productType, brand, series, model);
            if (destItem == null) {
                destItem = findInventoryItemByBrandSeries(destShop, productType, brand, series);
            }
            if (destItem != null) {
                destItem.setQuantity(destItem.getQuantity() + quantity);
                destItem.setUpdateTime(now);
                inventoryItemRepository.save(destItem);
                log.info("调货增加库存: 门店={}, 货品={} {}, 增加={}, 总数={}",
                        destShop, brand, series, quantity, destItem.getQuantity());
            } else {
                // 目标门店无此货品，用源门店信息新建
                InventoryItem newItem = new InventoryItem();
                newItem.setId(UUID.randomUUID().toString().replace("-", ""));
                newItem.setShopName(destShop);
                newItem.setProductType(productType);
                newItem.setBrand(brand);
                newItem.setSeries(series);
                newItem.setModel(model);
                newItem.setQuantity(quantity);
                newItem.setCreateTime(now);
                inventoryItemRepository.save(newItem);
                log.info("调货自动创建目标门店库存: 门店={}, 货品={} {}, 数量={}", destShop, brand, series, quantity);
            }
        } catch (Exception e) {
            log.error("同步库存失败，调货记录ID: {}, error: {}", record.getId(), e.getMessage(), e);
        }
    }

    private InventoryItem findExactInventoryItem(String shopName, String productType, String brand, String series, String model) {
        return inventoryItemRepository.findMatching(shopName, productType, brand, series,
                model != null && !model.isEmpty() ? model : null);
    }

    /**
     * 按门店+品类+品牌+系列查找库存（不要求型号匹配），优先取最先创建的记录。
     * 如果存在多条重复记录，自动合并到第一条并删除多余的。
     */
    private InventoryItem findInventoryItemByBrandSeries(String shopName, String productType, String brand, String series) {
        List<InventoryItem> items = inventoryItemRepository.findByBrandSeries(shopName, productType, brand, series);
        if (items.isEmpty()) return null;
        InventoryItem main = items.get(0);
        if (items.size() > 1) {
            int total = main.getQuantity();
            for (int i = 1; i < items.size(); i++) {
                total += items.get(i).getQuantity();
                inventoryItemRepository.delete(items.get(i));
            }
            main.setQuantity(total);
            main.setUpdateTime(LocalDateTime.now());
            inventoryItemRepository.save(main);
            log.info("合并重复库存: 门店={}, 品牌={}, 系列={}, 合并{}条, 总数={}",
                    shopName, brand, series, items.size(), total);
        }
        return items.get(0);
    }
}