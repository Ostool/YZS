package com.example.usermanagement.repository;

import com.example.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    List<User> findByRoleLevelGreaterThan(Integer roleLevel);

    @Query("SELECT u FROM User u WHERE u.id != :currentUserId ORDER BY u.createdAt DESC")
    List<User> findAllExceptCurrent(@Param("currentUserId") Integer currentUserId);
}