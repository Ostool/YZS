package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Integer> {

    // ========== 关键字搜索 ==========

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%'))")
    List<UserData> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.shopName = :shopName")
    List<UserData> searchByKeywordForNormalUser(@Param("keyword") String keyword, @Param("shopName") String shopName);

    // ========== 获取所有未删除数据（按配镜日期倒序） ==========

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 ORDER BY u.prescriptionDate DESC")
    List<UserData> findAllByOrderByPrescriptionDateDesc();

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findByShopNameOrderByPrescriptionDateDesc(@Param("shopName") String shopName);

    // ========== 日期范围查询 ==========

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY u.prescriptionDate DESC")
    List<UserData> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findByDateRangeForNormalUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("shopName") String shopName);

    // ========== 带关键字的日期范围查询 ==========

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.prescriptionDate BETWEEN :startDate AND :endDate AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) ORDER BY u.prescriptionDate DESC")
    List<UserData> searchByKeywordAndDateRange(@Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND u.shopName = :shopName AND u.prescriptionDate BETWEEN :startDate AND :endDate AND (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) ORDER BY u.prescriptionDate DESC")
    List<UserData> searchByKeywordAndDateRangeForNormalUser(@Param("keyword") String keyword, @Param("shopName") String shopName, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ========== 今日/本月/本年快捷查询 ==========

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND DATE(u.prescriptionDate) = CURRENT_DATE ORDER BY u.prescriptionDate DESC")
    List<UserData> findTodayData();

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND DATE(u.prescriptionDate) = CURRENT_DATE AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findTodayDataForNormalUser(@Param("shopName") String shopName);

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) AND MONTH(u.prescriptionDate) = MONTH(CURRENT_DATE) ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentMonthData();

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) AND MONTH(u.prescriptionDate) = MONTH(CURRENT_DATE) AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentMonthDataForNormalUser(@Param("shopName") String shopName);

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentYearData();

    @Query("SELECT u FROM UserData u WHERE u.isDeleted = 0 AND YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentYearDataForNormalUser(@Param("shopName") String shopName);
}