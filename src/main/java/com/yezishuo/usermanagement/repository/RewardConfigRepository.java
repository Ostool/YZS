package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.RewardConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RewardConfigRepository extends JpaRepository<RewardConfig, Integer> {
    Optional<RewardConfig> findByConfigKey(String configKey);
}