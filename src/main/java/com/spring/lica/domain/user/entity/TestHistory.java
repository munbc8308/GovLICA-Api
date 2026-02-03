package com.spring.lica.domain.user.entity;

import com.spring.lica.domain.catalog.entity.ApiCatalog;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_history", indexes = {
        @Index(name = "idx_history_user_id", columnList = "user_id"),
        @Index(name = "idx_history_executed_at", columnList = "executedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id")
    private ApiCatalog catalog;

    @Column(nullable = false, length = 1000)
    private String requestUrl;

    @Column(length = 2000)
    private String requestParams;

    @Column(columnDefinition = "CLOB")
    private String responseBody;

    private Integer responseStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        executedAt = LocalDateTime.now();
    }
}
