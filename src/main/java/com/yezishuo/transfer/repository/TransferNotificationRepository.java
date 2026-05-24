package com.yezishuo.transfer.repository;

import com.yezishuo.transfer.entity.TransferNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface TransferNotificationRepository extends JpaRepository<TransferNotification, String> {

    // 获取用户的通知，按时间倒序
    @Query("SELECT n FROM TransferNotification n WHERE n.userId = :userId ORDER BY n.createTime DESC")
    List<TransferNotification> findByUserIdOrderByCreateTimeDesc(@Param("userId") Integer userId);

    // 获取用户未读通知数量
    @Query("SELECT COUNT(n) FROM TransferNotification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") Integer userId);

    // 标记通知为已读
    @Modifying
    @Transactional
    @Query("UPDATE TransferNotification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") String id);

    // 标记用户所有通知为已读
    @Modifying
    @Transactional
    @Query("UPDATE TransferNotification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Integer userId);
}