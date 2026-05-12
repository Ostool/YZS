package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Integer> {
    @Query("SELECT DISTINCT i.period FROM Inspection i ORDER BY i.period DESC")
    List<String> findAllPeriods();

    @Query("SELECT i FROM Inspection i WHERE i.period = :period ORDER BY i.id ASC")
    List<Inspection> findByPeriod(@Param("period") String period);
}