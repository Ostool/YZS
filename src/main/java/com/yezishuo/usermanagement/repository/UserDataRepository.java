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

    // 普通搜索（超级管理员和游客用）
    @Query("SELECT u FROM UserData u WHERE " +
            "u.name LIKE CONCAT('%', :keyword, '%') OR " +
            "u.shopName LIKE CONCAT('%', :keyword, '%') OR " +
            "u.serialNo LIKE CONCAT('%', :keyword, '%') OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%') OR " +
            "u.glassesPurpose LIKE CONCAT('%', :keyword, '%')")
    List<UserData> searchByKeyword(@Param("keyword") String keyword);

    // 普通用户搜索（只能看到自己店名的数据）
    @Query("SELECT u FROM UserData u WHERE " +
            "(u.name LIKE CONCAT('%', :keyword, '%') OR " +
            "u.shopName LIKE CONCAT('%', :keyword, '%') OR " +
            "u.serialNo LIKE CONCAT('%', :keyword, '%') OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%') OR " +
            "u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND " +
            "u.shopName = :shopName")
    List<UserData> searchByKeywordForNormalUser(@Param("keyword") String keyword, @Param("shopName") String shopName);

    // 获取所有数据（超级管理员）
    List<UserData> findAllByOrderByPrescriptionDateDesc();

    // 普通用户获取自己店名的数据
    List<UserData> findByShopNameOrderByPrescriptionDateDesc(String shopName);

    // 按时间范围查询（超级管理员）
    @Query("SELECT u FROM UserData u WHERE u.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY u.prescriptionDate DESC")
    List<UserData> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 按时间范围查询（普通用户）
    @Query("SELECT u FROM UserData u WHERE u.prescriptionDate BETWEEN :startDate AND :endDate AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findByDateRangeForNormalUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("shopName") String shopName);

    // 获取当日数据（超级管理员）
    @Query("SELECT u FROM UserData u WHERE DATE(u.prescriptionDate) = CURRENT_DATE ORDER BY u.prescriptionDate DESC")
    List<UserData> findTodayData();

    // 获取当日数据（普通用户）
    @Query("SELECT u FROM UserData u WHERE DATE(u.prescriptionDate) = CURRENT_DATE AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findTodayDataForNormalUser(@Param("shopName") String shopName);

    // 获取当月数据（超级管理员）
    @Query("SELECT u FROM UserData u WHERE YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) AND MONTH(u.prescriptionDate) = MONTH(CURRENT_DATE) ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentMonthData();

    // 获取当月数据（普通用户）
    @Query("SELECT u FROM UserData u WHERE YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) AND MONTH(u.prescriptionDate) = MONTH(CURRENT_DATE) AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentMonthDataForNormalUser(@Param("shopName") String shopName);

    // 获取当年数据（超级管理员）
    @Query("SELECT u FROM UserData u WHERE YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentYearData();

    // 获取当年数据（普通用户）
    @Query("SELECT u FROM UserData u WHERE YEAR(u.prescriptionDate) = YEAR(CURRENT_DATE) AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> findCurrentYearDataForNormalUser(@Param("shopName") String shopName);

    @Query("SELECT u FROM UserData u WHERE (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.prescriptionDate BETWEEN :startDate AND :endDate ORDER BY u.prescriptionDate DESC")
    List<UserData> searchByKeywordAndDateRange(@Param("keyword") String keyword, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT u FROM UserData u WHERE (u.name LIKE CONCAT('%', :keyword, '%') OR u.shopName LIKE CONCAT('%', :keyword, '%') OR u.serialNo LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.glassesPurpose LIKE CONCAT('%', :keyword, '%')) AND u.prescriptionDate BETWEEN :startDate AND :endDate AND u.shopName = :shopName ORDER BY u.prescriptionDate DESC")
    List<UserData> searchByKeywordAndDateRangeForNormalUser(@Param("keyword") String keyword, @Param("shopName") String shopName, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}