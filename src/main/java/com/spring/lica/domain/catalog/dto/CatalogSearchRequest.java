package com.spring.lica.domain.catalog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogSearchRequest {
    private String keyword;
    private String category;
    private String org;
    private int page = 0;
    private int size = 12;
}
