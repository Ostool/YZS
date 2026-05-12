package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Integer> {
    @Query("SELECT r FROM Rule r ORDER BY r.sortOrder ASC, r.id ASC")
    List<Rule> findAllOrdered();
}