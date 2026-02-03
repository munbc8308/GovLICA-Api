package com.spring.lica.domain.proxy.service;

import com.spring.lica.client.datagokr.DataGoKrClient;
import com.spring.lica.domain.proxy.dto.ProxyRequest;
import com.spring.lica.domain.proxy.dto.ProxyResponse;
import com.spring.lica.domain.user.service.TestHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiProxyService {

    private final DataGoKrClient dataGoKrClient;
    private final TestHistoryService testHistoryService;

    @Value("${app.proxy.allowed-domains:apis.data.go.kr}")
    private String allowedDomains;

    public ProxyResponse execute(ProxyRequest request, String email) {
        validateTargetUrl(request.getTargetUrl());

        Map<String, String> allParams = new LinkedHashMap<>();
        allParams.put("serviceKey", request.getServiceKey());
        if (request.getParams() != null) {
            allParams.putAll(request.getParams());
        }

        String curl = buildCurl(request.getTargetUrl(), allParams);

        String paramsJson = allParams.entrySet().stream()
                .filter(e -> !"serviceKey".equals(e.getKey()))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        long start = System.currentTimeMillis();
        try {
            String responseBody = dataGoKrClient.proxyCall(request.getTargetUrl(), allParams);
            long elapsed = System.currentTimeMillis() - start;

            if (email != null) {
                testHistoryService.saveHistory(email, request.getTargetUrl(), paramsJson,
                        responseBody, 200, request.getUddiSeq());
            }

            return ProxyResponse.builder()
                    .status(200)
                    .body(responseBody)
                    .elapsedMs(elapsed)
                    .curl(curl)
                    .build();
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("Proxy execution failed: {}", e.getMessage());

            String errorBody = "{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}";
            if (email != null) {
                testHistoryService.saveHistory(email, request.getTargetUrl(), paramsJson,
                        errorBody, 500, request.getUddiSeq());
            }

            return ProxyResponse.builder()
                    .status(500)
                    .body(errorBody)
                    .elapsedMs(elapsed)
                    .curl(curl)
                    .build();
        }
    }

    private void validateTargetUrl(String targetUrl) {
        String[] domains = allowedDomains.split(",");
        boolean allowed = false;
        for (String domain : domains) {
            if (targetUrl.contains(domain.trim())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("허용되지 않은 도메인입니다. 허용 도메인: " + allowedDomains);
        }
    }

    private String buildCurl(String targetUrl, Map<String, String> params) {
        String queryString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String maskedQuery = params.entrySet().stream()
                .map(e -> {
                    if ("serviceKey".equals(e.getKey())) {
                        return e.getKey() + "=YOUR_SERVICE_KEY";
                    }
                    return e.getKey() + "=" + e.getValue();
                })
                .collect(Collectors.joining("&"));

        return "curl -X GET \"" + targetUrl + "?" + maskedQuery + "\"";
    }
}
