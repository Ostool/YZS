package com.yezishuo.transfer.repository;

import com.yezishuo.transfer.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, String> {

    // 按创建时间倒序 - 最新的在前
    @Query("SELECT t FROM TransferRecord t WHERE t.status = :status ORDER BY t.createTime DESC")
    List<TransferRecord> findByStatusOrderByCreateTimeDesc(@Param("status") String status);

    // 获取所有有效记录并按创建时间倒序
    @Query("SELECT t FROM TransferRecord t WHERE t.status = 'active' ORDER BY t.createTime DESC")
    List<TransferRecord> findAllActiveOrderByCreateTimeDesc();
}