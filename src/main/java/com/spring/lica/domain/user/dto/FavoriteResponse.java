package com.spring.lica.domain.user.dto;

import com.spring.lica.domain.catalog.entity.ApiCatalog;
import com.spring.lica.domain.user.entity.UserFavorite;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FavoriteResponse {
    private Long catalogId;
    private String uddiSeq;
    private String apiName;
    private String providerOrg;
    private String category;
    private LocalDateTime favoritedAt;

    public static FavoriteResponse from(UserFavorite entity) {
        ApiCatalog c = entity.getCatalog();
        return FavoriteResponse.builder()
                .catalogId(c.getId())
                .uddiSeq(c.getUddiSeq())
                .apiName(c.getApiName())
                .providerOrg(c.getProviderOrg())
                .category(c.getCategory())
                .favoritedAt(entity.getCreatedAt())
                .build();
    }
}
