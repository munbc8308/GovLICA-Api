package com.spring.lica.domain.catalog.dto;

import com.spring.lica.domain.catalog.entity.ApiCatalog;
import com.spring.lica.domain.catalog.entity.ApiOperation;
import com.spring.lica.domain.catalog.entity.ApiParameter;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CatalogDetailResponse {

    private Long id;
    private String uddiSeq;
    private String apiName;
    private String description;
    private String providerOrg;
    private String category;
    private String serviceType;
    private String dataFormat;
    private String endpointUrl;
    private List<OperationDto> operations;

    @Getter
    @Builder
    public static class OperationDto {
        private Long id;
        private String operationName;
        private String httpMethod;
        private String endpointUrl;
        private String description;
        private List<ParameterDto> requestParams;
        private List<ParameterDto> responseFields;
    }

    @Getter
    @Builder
    public static class ParameterDto {
        private String paramName;
        private String paramType;
        private boolean required;
        private String description;
        private String defaultValue;
    }

    public static CatalogDetailResponse from(ApiCatalog catalog) {
        List<OperationDto> ops = catalog.getOperations().stream()
                .map(CatalogDetailResponse::toOperationDto)
                .toList();

        return CatalogDetailResponse.builder()
                .id(catalog.getId())
                .uddiSeq(catalog.getUddiSeq())
                .apiName(catalog.getApiName())
                .description(catalog.getDescription())
                .providerOrg(catalog.getProviderOrg())
                .category(catalog.getCategory())
                .serviceType(catalog.getServiceType())
                .dataFormat(catalog.getDataFormat())
                .endpointUrl(catalog.getEndpointUrl())
                .operations(ops)
                .build();
    }

    private static OperationDto toOperationDto(ApiOperation op) {
        List<ParameterDto> reqParams = op.getParameters().stream()
                .filter(p -> p.getDirection() == ApiParameter.Direction.REQUEST)
                .map(CatalogDetailResponse::toParameterDto)
                .toList();
        List<ParameterDto> resFields = op.getParameters().stream()
                .filter(p -> p.getDirection() == ApiParameter.Direction.RESPONSE)
                .map(CatalogDetailResponse::toParameterDto)
                .toList();

        return OperationDto.builder()
                .id(op.getId())
                .operationName(op.getOperationName())
                .httpMethod(op.getHttpMethod())
                .endpointUrl(op.getEndpointUrl())
                .description(op.getDescription())
                .requestParams(reqParams)
                .responseFields(resFields)
                .build();
    }

    private static ParameterDto toParameterDto(ApiParameter p) {
        return ParameterDto.builder()
                .paramName(p.getParamName())
                .paramType(p.getParamType())
                .required(p.isRequired())
                .description(p.getDescription())
                .defaultValue(p.getDefaultValue())
                .build();
    }
}
