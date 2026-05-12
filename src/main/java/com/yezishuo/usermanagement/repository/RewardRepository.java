package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {
    @Query("SELECT r FROM Reward r ORDER BY r.sortOrder ASC, r.id ASC")
    List<Reward> findAllOrdered();
}