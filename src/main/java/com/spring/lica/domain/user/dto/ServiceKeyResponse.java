package com.spring.lica.domain.user.dto;

import com.spring.lica.domain.user.entity.UserServiceKey;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ServiceKeyResponse {
    private Long id;
    private String keyName;
    private String maskedKey;
    private LocalDateTime createdAt;

    public static ServiceKeyResponse from(UserServiceKey entity, String decryptedKey) {
        return ServiceKeyResponse.builder()
                .id(entity.getId())
                .keyName(entity.getKeyName())
                .maskedKey(maskKey(decryptedKey))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static String maskKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}
