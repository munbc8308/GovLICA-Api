package com.spring.lica.domain.catalog.controller;

import com.spring.lica.domain.catalog.dto.CatalogDetailResponse;
import com.spring.lica.domain.catalog.dto.CatalogResponse;
import com.spring.lica.domain.catalog.dto.CatalogSearchRequest;
import com.spring.lica.domain.catalog.service.CatalogDetailService;
import com.spring.lica.domain.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CatalogViewController {

    private final CatalogService catalogService;
    private final CatalogDetailService catalogDetailService;

    @GetMapping("/")
    public String index(CatalogSearchRequest request,
                        @RequestParam(defaultValue = "portal") String source,
                        Model model) {
        Page<CatalogResponse> results;
        if ("local".equals(source)) {
            results = catalogService.searchFromLocal(request);
        } else {
            results = catalogService.searchFromPortal(request);
        }
        model.addAttribute("results", results);
        model.addAttribute("request", request);
        model.addAttribute("source", source);
        model.addAttribute("localCount", catalogService.getRestApiCount());
        return "index";
    }

    @GetMapping("/explore/{uddiSeq}")
    public String explore(@PathVariable String uddiSeq,
                          @RequestParam(defaultValue = "false") boolean refresh,
                          Model model) {
        CatalogDetailResponse detail;
        if (refresh) {
            detail = catalogDetailService.refreshDetail(uddiSeq);
        } else {
            detail = catalogDetailService.getDetail(uddiSeq);
        }
        model.addAttribute("detail", detail);
        return "explore";
    }

    @GetMapping("/console/{uddiSeq}")
    public String console(@PathVariable String uddiSeq, Model model) {
        CatalogDetailResponse detail = catalogDetailService.getDetail(uddiSeq);
        model.addAttribute("detail", detail);
        return "console";
    }
}
