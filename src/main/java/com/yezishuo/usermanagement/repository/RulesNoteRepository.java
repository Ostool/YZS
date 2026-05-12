package com.yezishuo.usermanagement.repository;

import com.yezishuo.usermanagement.entity.RulesNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RulesNoteRepository extends JpaRepository<RulesNote, Integer> {
}