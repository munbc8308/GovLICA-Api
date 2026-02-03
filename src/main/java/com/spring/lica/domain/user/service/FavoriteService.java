package com.spring.lica.domain.user.service;

import com.spring.lica.common.exception.UserNotFoundException;
import com.spring.lica.domain.catalog.entity.ApiCatalog;
import com.spring.lica.domain.catalog.repository.ApiCatalogRepository;
import com.spring.lica.domain.user.dto.FavoriteResponse;
import com.spring.lica.domain.user.entity.User;
import com.spring.lica.domain.user.entity.UserFavorite;
import com.spring.lica.domain.user.repository.UserFavoriteRepository;
import com.spring.lica.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ApiCatalogRepository catalogRepository;

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(String email) {
        User user = findUser(email);
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    @Transactional
    public FavoriteResponse addFavorite(String email, Long catalogId) {
        User user = findUser(email);
        if (favoriteRepository.existsByUserIdAndCatalogId(user.getId(), catalogId)) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 API입니다");
        }
        ApiCatalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new IllegalArgumentException("API를 찾을 수 없습니다"));
        UserFavorite fav = UserFavorite.builder()
                .user(user)
                .catalog(catalog)
                .build();
        UserFavorite saved = favoriteRepository.save(fav);
        return FavoriteResponse.from(saved);
    }

    @Transactional
    public void removeFavorite(String email, Long catalogId) {
        User user = findUser(email);
        favoriteRepository.deleteByUserIdAndCatalogId(user.getId(), catalogId);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String email, Long catalogId) {
        User user = findUser(email);
        return favoriteRepository.existsByUserIdAndCatalogId(user.getId(), catalogId);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
    }
}
