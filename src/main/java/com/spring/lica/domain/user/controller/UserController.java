package com.spring.lica.domain.user.controller;

import com.spring.lica.domain.user.dto.*;
import com.spring.lica.domain.user.service.FavoriteService;
import com.spring.lica.domain.user.service.ServiceKeyService;
import com.spring.lica.domain.user.service.TestHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ServiceKeyService serviceKeyService;
    private final FavoriteService favoriteService;
    private final TestHistoryService testHistoryService;

    // ===== Service Keys =====

    @GetMapping("/keys")
    public ResponseEntity<List<ServiceKeyResponse>> getKeys(Authentication auth) {
        return ResponseEntity.ok(serviceKeyService.getKeys(auth.getName()));
    }

    @PostMapping("/keys")
    public ResponseEntity<ServiceKeyResponse> addKey(Authentication auth,
                                                     @Valid @RequestBody ServiceKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceKeyService.addKey(auth.getName(), request));
    }

    @DeleteMapping("/keys/{id}")
    public ResponseEntity<Void> deleteKey(Authentication auth, @PathVariable Long id) {
        serviceKeyService.deleteKey(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/keys/{id}/decrypt")
    public ResponseEntity<Map<String, String>> decryptKey(Authentication auth, @PathVariable Long id) {
        String decrypted = serviceKeyService.decryptKey(auth.getName(), id);
        return ResponseEntity.ok(Map.of("serviceKey", decrypted));
    }

    // ===== Favorites =====

    @GetMapping("/favorites")
    public ResponseEntity<List<FavoriteResponse>> getFavorites(Authentication auth) {
        return ResponseEntity.ok(favoriteService.getFavorites(auth.getName()));
    }

    @PostMapping("/favorites/{catalogId}")
    public ResponseEntity<FavoriteResponse> addFavorite(Authentication auth, @PathVariable Long catalogId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavorite(auth.getName(), catalogId));
    }

    @DeleteMapping("/favorites/{catalogId}")
    public ResponseEntity<Void> removeFavorite(Authentication auth, @PathVariable Long catalogId) {
        favoriteService.removeFavorite(auth.getName(), catalogId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites/check/{catalogId}")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(Authentication auth, @PathVariable Long catalogId) {
        boolean isFav = favoriteService.isFavorite(auth.getName(), catalogId);
        return ResponseEntity.ok(Map.of("favorite", isFav));
    }

    // ===== History =====

    @GetMapping("/history")
    public ResponseEntity<Page<TestHistoryResponse>> getHistory(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(testHistoryService.getHistory(auth.getName(), page, size));
    }
}
