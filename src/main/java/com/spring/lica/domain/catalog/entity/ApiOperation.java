package com.spring.lica.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "api_operation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private ApiCatalog catalog;

    @Column(nullable = false)
    private String operationName;

    private String httpMethod;

    @Column(length = 1000)
    private String endpointUrl;

    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApiParameter> parameters = new ArrayList<>();
}
