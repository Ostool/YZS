package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.FundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FundItemRepository extends JpaRepository<FundItem, Integer> {
    @Query("SELECT f FROM FundItem f ORDER BY f.createdAt DESC")
    List<FundItem> findAllOrdered();
}