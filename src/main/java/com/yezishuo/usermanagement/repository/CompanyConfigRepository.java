package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.CompanyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyConfigRepository extends JpaRepository<CompanyConfig, Integer> {
    Optional<CompanyConfig> findByConfigKey(String configKey);
}