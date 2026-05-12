package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.InspectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InspectConfigRepository extends JpaRepository<InspectConfig, Integer> {
    Optional<InspectConfig> findByConfigKey(String configKey);
}