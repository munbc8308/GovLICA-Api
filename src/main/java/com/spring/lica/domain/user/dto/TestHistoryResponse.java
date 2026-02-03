package com.spring.lica.domain.user.dto;

import com.spring.lica.domain.user.entity.TestHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TestHistoryResponse {
    private Long id;
    private String catalogName;
    private String requestUrl;
    private Integer responseStatus;
    private LocalDateTime executedAt;

    public static TestHistoryResponse from(TestHistory entity) {
        return TestHistoryResponse.builder()
                .id(entity.getId())
                .catalogName(entity.getCatalog() != null ? entity.getCatalog().getApiName() : null)
                .requestUrl(entity.getRequestUrl())
                .responseStatus(entity.getResponseStatus())
                .executedAt(entity.getExecutedAt())
                .build();
    }
}
