package com.spring.lica.domain.catalog.dto;

import com.spring.lica.domain.catalog.entity.ApiCatalog;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CatalogResponse {
    private Long id;
    private String uddiSeq;
    private String apiName;
    private String description;
    private String providerOrg;
    private String category;
    private String serviceType;
    private String dataFormat;
    private String endpointUrl;

    public static CatalogResponse from(ApiCatalog entity) {
        return CatalogResponse.builder()
                .id(entity.getId())
                .uddiSeq(entity.getUddiSeq())
                .apiName(entity.getApiName())
                .description(entity.getDescription())
                .providerOrg(entity.getProviderOrg())
                .category(entity.getCategory())
                .serviceType(entity.getServiceType())
                .dataFormat(entity.getDataFormat())
                .endpointUrl(entity.getEndpointUrl())
                .build();
    }

    public String shortDescription() {
        if (description == null) return "";
        return description.length() > 100 ? description.substring(0, 100) + "..." : description;
    }
}
