package com.example.usermanagement.repository;

import com.example.usermanagement.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserDataRepository extends JpaRepository<UserData, Integer> {
    @Query("SELECT u FROM UserData u WHERE " +
            "u.name LIKE CONCAT('%', :keyword, '%') OR " +
            "u.shopName LIKE CONCAT('%', :keyword, '%') OR " +
            "u.serialNo LIKE CONCAT('%', :keyword, '%')")
    List<UserData> searchByKeyword(@Param("keyword") String keyword);
}