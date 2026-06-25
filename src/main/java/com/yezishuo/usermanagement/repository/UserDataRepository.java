package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.UserData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Integer> {

    // ========== 分页查询（主数据加载） ==========

    @Query(value = "SELECT u FROM UserData u WHERE u.isDeleted = 0 ORDER BY u.prescriptionDate DESC",
           countQuery = "SELECT COUNT(u) FROM UserData u WHERE u.isDeleted = 0")
    Page<UserData> findAllActivePaged(Pageable pageable);

    @Query(value = "SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC",
           countQuery = "SELECT COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName")
    Page<UserData> findByShopNameActivePaged(@Param("shopName") String shopName, Pageable pageable);

    // ========== 关键字搜索（分页） ==========

    @Query(value = "SELECT u FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) ORDER BY u.prescriptionDate DESC",
           countQuery = "SELECT COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%'))")
    Page<UserData> searchByKeywordPaged(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT u FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC",
           countQuery = "SELECT COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.shopName = :shopName")
    Page<UserData> searchByKeywordForNormalUserPaged(@Param("keyword") String keyword, @Param("shopName") String shopName, Pageable pageable);

    // ========== 日期范围查询（分页） ==========

    @Query(value = "SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY u.prescriptionDate DESC",
           countQuery = "SELECT COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate")
    Page<UserData> findByDateRangePaged(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query(value = "SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC",
           countQuery = "SELECT COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate AND u.shopName = :shopName")
    Page<UserData> findByDateRangeForNormalUserPaged(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("shopName") String shopName, Pageable pageable);

    // ========== 统计聚合查询（优化：单次查询替代三次查询） ==========

    @Query("SELECT SUM(u.totalAmount), COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :start AND :end")
    List<Object[]> aggregateStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(u.totalAmount), COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName AND u.prescriptionDate BETWEEN :start AND :end")
    List<Object[]> aggregateStatsForNormalUser(@Param("shopName") String shopName, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(u.totalAmount), COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.prescriptionDate BETWEEN :start AND :end")
    List<Object[]> aggregateStatsWithKeyword(@Param("keyword") String keyword, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(u.totalAmount), COUNT(u) FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.prescriptionDate BETWEEN :start AND :end")
    List<Object[]> aggregateStatsWithKeywordForNormalUser(@Param("keyword") String keyword, @Param("shopName") String shopName, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ========== 以下为保留的旧方法（处方新增/删除等内部调用） ==========

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 ORDER BY u.prescriptionDate DESC")
    java.util.List<UserData> findAllByOrderByPrescriptionDateDesc();

    // ========== 销售之星 ==========

    @Query("SELECT u.consultant, COUNT(u), COALESCE(SUM(u.totalAmount), 0) FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :start AND :end GROUP BY u.consultant ORDER BY COUNT(u) DESC")
    List<Object[]> findSalesStars(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.consultant, COUNT(u), COALESCE(SUM(u.totalAmount), 0) FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName AND u.prescriptionDate BETWEEN :start AND :end GROUP BY u.consultant ORDER BY COUNT(u) DESC")
    List<Object[]> findSalesStarsByShop(@Param("shopName") String shopName, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ========== 服务之星（线上订单 + 好评） ==========

    @Query("SELECT u.consultant, COUNT(u), COALESCE(SUM(CASE WHEN u.hasGoodReview = 1 THEN 1 ELSE 0 END), 0) FROM UserData u WHERE u.isDeleted = 0 AND u.redemptionChannel IS NOT NULL AND u.redemptionChannel != '' AND u.prescriptionDate BETWEEN :start AND :end GROUP BY u.consultant ORDER BY COUNT(u) DESC")
    List<Object[]> findServiceStars(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.consultant, COUNT(u), COALESCE(SUM(CASE WHEN u.hasGoodReview = 1 THEN 1 ELSE 0 END), 0) FROM UserData u WHERE u.isDeleted = 0 AND u.redemptionChannel IS NOT NULL AND u.redemptionChannel != '' AND u.shopName = :shopName AND u.prescriptionDate BETWEEN :start AND :end GROUP BY u.consultant ORDER BY COUNT(u) DESC")
    List<Object[]> findServiceStarsByShop(@Param("shopName") String shopName, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
