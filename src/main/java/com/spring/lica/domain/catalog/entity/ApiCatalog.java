package com.spring.lica.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "api_catalog", indexes = {
        @Index(name = "idx_catalog_uddi_seq", columnList = "uddiSeq", unique = true),
        @Index(name = "idx_catalog_service_type", columnList = "serviceType"),
        @Index(name = "idx_catalog_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uddiSeq;

    @Column(nullable = false)
    private String apiName;

    @Column(length = 2000)
    private String description;

    private String providerOrg;

    private String category;

    @Column(nullable = false)
    private String serviceType;

    private String dataFormat;

    @Column(length = 1000)
    private String endpointUrl;

    private LocalDateTime lastSyncedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "catalog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApiOperation> operations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
