package com.spring.lica.domain.catalog.service;

import com.spring.lica.client.datagokr.DataGoKrClient;
import com.spring.lica.client.datagokr.dto.ApiDetailParseResult;
import com.spring.lica.domain.catalog.dto.CatalogDetailResponse;
import com.spring.lica.domain.catalog.entity.ApiCatalog;
import com.spring.lica.domain.catalog.entity.ApiOperation;
import com.spring.lica.domain.catalog.entity.ApiParameter;
import com.spring.lica.domain.catalog.repository.ApiCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogDetailService {

    private final DataGoKrClient dataGoKrClient;
    private final ApiCatalogRepository catalogRepository;

    /**
     * API 상세 정보를 조회한다.
     * 1) 로컬 DB에 오퍼레이션 정보가 있으면 캐시에서 반환
     * 2) 없으면 data.go.kr에서 스크래핑 후 DB에 캐싱하고 반환
     */
    @Transactional
    public CatalogDetailResponse getDetail(String uddiSeq) {
        Optional<ApiCatalog> existing = catalogRepository.findByUddiSeq(uddiSeq);

        if (existing.isPresent() && !existing.get().getOperations().isEmpty()
                && existing.get().getOperations().stream().anyMatch(op -> !op.getParameters().isEmpty())) {
            log.debug("Returning cached detail for uddiSeq={}", uddiSeq);
            return CatalogDetailResponse.from(existing.get());
        }

        // data.go.kr에서 스크래핑
        log.info("Fetching detail from data.go.kr for uddiSeq={}", uddiSeq);
        ApiDetailParseResult parsed = dataGoKrClient.fetchApiDetail(uddiSeq);
        ApiCatalog catalog = saveOrUpdateFromParsed(uddiSeq, parsed, existing.orElse(null));

        return CatalogDetailResponse.from(catalog);
    }

    /**
     * 캐시를 무시하고 data.go.kr에서 강제 새로고침
     */
    @Transactional
    public CatalogDetailResponse refreshDetail(String uddiSeq) {
        log.info("Force refreshing detail from data.go.kr for uddiSeq={}", uddiSeq);
        Optional<ApiCatalog> existing = catalogRepository.findByUddiSeq(uddiSeq);

        // 기존 오퍼레이션 제거 후 flush하여 orphan 삭제 확정
        if (existing.isPresent()) {
            existing.get().getOperations().clear();
            catalogRepository.flush();
        }

        ApiDetailParseResult parsed = dataGoKrClient.fetchApiDetail(uddiSeq);
        ApiCatalog catalog = saveOrUpdateFromParsed(uddiSeq, parsed, existing.orElse(null));

        return CatalogDetailResponse.from(catalog);
    }

    private ApiCatalog saveOrUpdateFromParsed(String uddiSeq, ApiDetailParseResult parsed, ApiCatalog existing) {
        ApiCatalog catalog = existing;
        if (catalog == null) {
            catalog = ApiCatalog.builder()
                    .uddiSeq(uddiSeq)
                    .apiName(parsed.getApiName())
                    .description(parsed.getDescription())
                    .providerOrg(parsed.getProviderOrg())
                    .category(parsed.getCategory())
                    .serviceType(parsed.getApiType() != null ? parsed.getApiType() : "REST")
                    .dataFormat(parsed.getDataFormat())
                    .endpointUrl(parsed.getServiceUrl())
                    .lastSyncedAt(LocalDateTime.now())
                    .build();
        } else {
            if (parsed.getApiName() != null) catalog.setApiName(parsed.getApiName());
            if (parsed.getDescription() != null) catalog.setDescription(parsed.getDescription());
            if (parsed.getProviderOrg() != null) catalog.setProviderOrg(parsed.getProviderOrg());
            if (parsed.getCategory() != null) catalog.setCategory(parsed.getCategory());
            if (parsed.getDataFormat() != null) catalog.setDataFormat(parsed.getDataFormat());
            if (parsed.getServiceUrl() != null) catalog.setEndpointUrl(parsed.getServiceUrl());
            catalog.setLastSyncedAt(LocalDateTime.now());
            catalog.getOperations().clear();
        }

        // 오퍼레이션 매핑
        for (ApiDetailParseResult.OperationInfo opInfo : parsed.getOperations()) {
            ApiOperation operation = ApiOperation.builder()
                    .catalog(catalog)
                    .operationName(opInfo.getOperationName())
                    .httpMethod(opInfo.getHttpMethod() != null ? opInfo.getHttpMethod() : "GET")
                    .endpointUrl(opInfo.getEndpointUrl())
                    .description(opInfo.getOperationName())
                    .build();

            // 요청 파라미터
            for (ApiDetailParseResult.ParameterInfo pi : opInfo.getRequestParams()) {
                ApiParameter param = ApiParameter.builder()
                        .operation(operation)
                        .paramName(pi.getNameEng() != null && !pi.getNameEng().isBlank() ? pi.getNameEng() : pi.getNameKor())
                        .paramType(pi.getSize())
                        .required("필".equals(pi.getDivision()) || "필수".equals(pi.getDivision()))
                        .description(buildParamDescription(pi))
                        .defaultValue(pi.getSampleData())
                        .direction(ApiParameter.Direction.REQUEST)
                        .build();
                operation.getParameters().add(param);
            }

            // 응답 필드
            for (ApiDetailParseResult.ParameterInfo pi : opInfo.getResponseFields()) {
                ApiParameter param = ApiParameter.builder()
                        .operation(operation)
                        .paramName(pi.getNameEng() != null && !pi.getNameEng().isBlank() ? pi.getNameEng() : pi.getNameKor())
                        .paramType(pi.getSize())
                        .required(false)
                        .description(buildParamDescription(pi))
                        .defaultValue(pi.getSampleData())
                        .direction(ApiParameter.Direction.RESPONSE)
                        .build();
                operation.getParameters().add(param);
            }

            catalog.getOperations().add(operation);
        }

        return catalogRepository.save(catalog);
    }

    private String buildParamDescription(ApiDetailParseResult.ParameterInfo pi) {
        StringBuilder sb = new StringBuilder();
        if (pi.getNameKor() != null && !pi.getNameKor().isBlank()) {
            sb.append(pi.getNameKor());
        }
        if (pi.getDescription() != null && !pi.getDescription().isBlank()
                && !pi.getDescription().equals(pi.getNameKor())) {
            if (!sb.isEmpty()) sb.append(" - ");
            sb.append(pi.getDescription());
        }
        return sb.toString();
    }
}
