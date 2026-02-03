package com.spring.lica.domain.user.repository;

import com.spring.lica.domain.user.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<UserFavorite> findByUserIdAndCatalogId(Long userId, Long catalogId);
    boolean existsByUserIdAndCatalogId(Long userId, Long catalogId);
    void deleteByUserIdAndCatalogId(Long userId, Long catalogId);
}
