package com.spring.lica.domain.user.repository;

import com.spring.lica.domain.user.entity.UserServiceKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserServiceKeyRepository extends JpaRepository<UserServiceKey, Long> {
    List<UserServiceKey> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<UserServiceKey> findByIdAndUserId(Long id, Long userId);
}
