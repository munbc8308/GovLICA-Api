package com.spring.lica.domain.catalog.service;

import com.spring.lica.client.datagokr.DataGoKrClient;
import com.spring.lica.client.datagokr.dto.PortalApiResponse;
import com.spring.lica.domain.catalog.dto.CatalogResponse;
import com.spring.lica.domain.catalog.dto.CatalogSearchRequest;
import com.spring.lica.domain.catalog.entity.ApiCatalog;
import com.spring.lica.domain.catalog.repository.ApiCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final ApiCatalogRepository catalogRepository;
    private final DataGoKrClient dataGoKrClient;

    /**
     * data.go.kr 포털에서 실시간으로 REST API를 검색한다.
     * 인증키 없이 웹 스크래핑 방식으로 조회.
     */
    public Page<CatalogResponse> searchFromPortal(CatalogSearchRequest request) {
        try {
            // data.go.kr은 1-based page
            int portalPage = request.getPage() + 1;
            PortalApiResponse response = dataGoKrClient.searchRestApis(
                    portalPage, request.getSize(), request.getKeyword(), request.getCategory());

            if (response == null || response.getResponse() == null
                    || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null
                    || response.getResponse().getBody().getItems().getItem() == null) {
                return new PageImpl<>(Collections.emptyList(), PageRequest.of(request.getPage(), request.getSize()), 0);
            }

            var body = response.getResponse().getBody();
            List<CatalogResponse> items = body.getItems().getItem().stream()
                    .filter(item -> item.getUddiSeq() != null)
                    .map(this::toResponse)
                    .toList();

            return new PageImpl<>(items, PageRequest.of(request.getPage(), request.getSize()), body.getTotalCount());
        } catch (Exception e) {
            log.error("Portal search failed, falling back to local DB", e);
            return searchFromLocal(request);
        }
    }

    /**
     * 로컬 DB에서 검색 (동기화된 데이터 대상)
     */
    public Page<CatalogResponse> searchFromLocal(CatalogSearchRequest request) {
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<ApiCatalog> page = catalogRepository.searchRestApis(
                request.getKeyword(), request.getCategory(), request.getOrg(), pageable);
        return page.map(CatalogResponse::from);
    }

    public Optional<CatalogResponse> findByUddiSeq(String uddiSeq) {
        return catalogRepository.findByUddiSeq(uddiSeq)
                .map(CatalogResponse::from);
    }

    public List<String> getCategories() {
        return catalogRepository.findDistinctCategories();
    }

    public List<String> getProviderOrgs() {
        return catalogRepository.findDistinctProviderOrgs();
    }

    public long getRestApiCount() {
        return catalogRepository.countByServiceType("REST");
    }

    private CatalogResponse toResponse(PortalApiResponse.ApiItem item) {
        return CatalogResponse.builder()
                .uddiSeq(item.getUddiSeq())
                .apiName(item.resolvedName())
                .description(item.resolvedDescription())
                .providerOrg(item.resolvedOrg())
                .category(item.resolvedCategory())
                .serviceType("REST")
                .dataFormat(item.resolvedDataFormat())
                .endpointUrl(item.resolvedEndpointUrl())
                .build();
    }
}
