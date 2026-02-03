package com.spring.lica.domain.user.repository;

import com.spring.lica.domain.user.entity.TestHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TestHistoryRepository extends JpaRepository<TestHistory, Long> {
    Page<TestHistory> findByUserIdOrderByExecutedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(h) FROM TestHistory h WHERE h.executedAt >= :since")
    long countSince(@Param("since") LocalDateTime since);
}
