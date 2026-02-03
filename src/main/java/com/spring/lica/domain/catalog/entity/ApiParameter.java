package com.spring.lica.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "api_parameter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private ApiOperation operation;

    @Column(nullable = false)
    private String paramName;

    private String paramType;

    private boolean required;

    @Column(length = 500)
    private String description;

    private String defaultValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    public enum Direction {
        REQUEST, RESPONSE
    }
}
