package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.FundConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FundConfigRepository extends JpaRepository<FundConfig, Integer> {
    Optional<FundConfig> findByConfigKey(String configKey);
}