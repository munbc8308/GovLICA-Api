package com.spring.lica.domain.proxy.controller;

import com.spring.lica.domain.proxy.dto.ProxyRequest;
import com.spring.lica.domain.proxy.dto.ProxyResponse;
import com.spring.lica.domain.proxy.service.ApiProxyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
public class ProxyController {

    private final ApiProxyService apiProxyService;

    @PostMapping("/execute")
    public ResponseEntity<ProxyResponse> execute(@Valid @RequestBody ProxyRequest request,
                                                  Authentication authentication) {
        String email = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(apiProxyService.execute(request, email));
    }
}
