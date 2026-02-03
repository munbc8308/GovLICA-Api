package com.spring.lica.domain.catalog.controller;

import com.spring.lica.domain.catalog.dto.CatalogDetailResponse;
import com.spring.lica.domain.catalog.dto.CatalogResponse;
import com.spring.lica.domain.catalog.dto.CatalogSearchRequest;
import com.spring.lica.domain.catalog.service.CatalogDetailService;
import com.spring.lica.domain.catalog.service.CatalogService;
import com.spring.lica.domain.catalog.service.CatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;
    private final CatalogSyncService catalogSyncService;
    private final CatalogDetailService catalogDetailService;

    @GetMapping("/search")
    public ResponseEntity<Page<CatalogResponse>> search(
            CatalogSearchRequest request,
            @RequestParam(defaultValue = "portal") String source) {
        Page<CatalogResponse> results;
        if ("local".equals(source)) {
            results = catalogService.searchFromLocal(request);
        } else {
            results = catalogService.searchFromPortal(request);
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{uddiSeq}")
    public ResponseEntity<CatalogDetailResponse> detail(@PathVariable String uddiSeq) {
        return ResponseEntity.ok(catalogDetailService.getDetail(uddiSeq));
    }

    @PostMapping("/{uddiSeq}/refresh")
    public ResponseEntity<CatalogDetailResponse> refresh(@PathVariable String uddiSeq) {
        return ResponseEntity.ok(catalogDetailService.refreshDetail(uddiSeq));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(catalogService.getCategories());
    }

    @GetMapping("/orgs")
    public ResponseEntity<List<String>> orgs() {
        return ResponseEntity.ok(catalogService.getProviderOrgs());
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync() {
        CatalogSyncService.SyncResult result = catalogSyncService.syncFromPortal();
        return ResponseEntity.ok(Map.of(
                "totalSynced", result.totalSynced(),
                "newApis", result.totalNew(),
                "updatedApis", result.totalUpdated(),
                "portalTotalCount", result.portalTotalCount()
        ));
    }
}
