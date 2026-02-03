package com.spring.lica.domain.catalog.service;

import com.spring.lica.client.datagokr.DataGoKrClient;
import com.spring.lica.client.datagokr.dto.PortalApiResponse;
import com.spring.lica.domain.catalog.entity.ApiCatalog;
import com.spring.lica.domain.catalog.repository.ApiCatalogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private final DataGoKrClient dataGoKrClient;
    private final ApiCatalogRepository catalogRepository;

    @Value("${app.catalog.sync-page-size:30}")
    private int syncPageSize;

    /**
     * data.go.kr 웹 검색 결과를 스크래핑하여 로컬 DB에 동기화한다.
     * 인증키 불필요 — 웹 페이지 HTML 파싱 방식.
     */
    @Transactional
    public SyncResult syncFromPortal() {
        log.info("Starting catalog sync from data.go.kr (HTML scraping)...");
        int totalSynced = 0;
        int totalNew = 0;
        int totalUpdated = 0;
        int portalTotalCount = 0;
        int page = 1;
        int maxPages = 10; // 안전 제한

        try {
            do {
                PortalApiResponse response = dataGoKrClient.searchRestApis(page, syncPageSize, "");
                if (response == null || response.getResponse() == null
                        || response.getResponse().getBody() == null) {
                    log.warn("Empty response at page {}", page);
                    break;
                }

                var body = response.getResponse().getBody();
                if (page == 1) {
                    portalTotalCount = body.getTotalCount();
                }

                if (body.getItems() == null || body.getItems().getItem() == null
                        || body.getItems().getItem().isEmpty()) {
                    break;
                }

                List<PortalApiResponse.ApiItem> items = body.getItems().getItem();
                for (PortalApiResponse.ApiItem item : items) {
                    String uddiSeq = item.getUddiSeq();
                    if (uddiSeq == null || uddiSeq.isBlank()) continue;

                    Optional<ApiCatalog> existing = catalogRepository.findByUddiSeq(uddiSeq);
                    if (existing.isPresent()) {
                        updateCatalog(existing.get(), item);
                        totalUpdated++;
                    } else {
                        createCatalog(item);
                        totalNew++;
                    }
                    totalSynced++;
                }

                log.info("Synced page {}: {} items (total so far: {})", page, items.size(), totalSynced);
                page++;

                // 요청 간격 (포털에 부하 방지)
                Thread.sleep(500);

            } while (page <= maxPages);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sync interrupted at page {}", page);
        } catch (Exception e) {
            log.error("Catalog sync failed at page {}", page, e);
        }

        log.info("Catalog sync completed: total={}, new={}, updated={}, portalTotal={}",
                totalSynced, totalNew, totalUpdated, portalTotalCount);
        return new SyncResult(totalSynced, totalNew, totalUpdated, portalTotalCount);
    }

    private void createCatalog(PortalApiResponse.ApiItem item) {
        ApiCatalog catalog = ApiCatalog.builder()
                .uddiSeq(item.getUddiSeq())
                .apiName(item.resolvedName())
                .description(item.resolvedDescription())
                .providerOrg(item.resolvedOrg())
                .category(item.resolvedCategory())
                .serviceType("REST")
                .dataFormat(item.resolvedDataFormat())
                .endpointUrl(item.resolvedEndpointUrl())
                .lastSyncedAt(LocalDateTime.now())
                .build();
        catalogRepository.save(catalog);
    }

    private void updateCatalog(ApiCatalog catalog, PortalApiResponse.ApiItem item) {
        if (item.resolvedName() != null) catalog.setApiName(item.resolvedName());
        if (item.resolvedDescription() != null) catalog.setDescription(item.resolvedDescription());
        if (item.resolvedOrg() != null) catalog.setProviderOrg(item.resolvedOrg());
        if (item.resolvedCategory() != null) catalog.setCategory(item.resolvedCategory());
        if (item.resolvedDataFormat() != null) catalog.setDataFormat(item.resolvedDataFormat());
        if (item.resolvedEndpointUrl() != null) catalog.setEndpointUrl(item.resolvedEndpointUrl());
        catalog.setServiceType("REST");
        catalog.setLastSyncedAt(LocalDateTime.now());
        catalogRepository.save(catalog);
    }

    public record SyncResult(int totalSynced, int totalNew, int totalUpdated, int portalTotalCount) {}
}
