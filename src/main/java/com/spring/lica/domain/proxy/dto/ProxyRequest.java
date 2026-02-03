package com.spring.lica.domain.proxy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ProxyRequest {

    @NotBlank(message = "대상 URL은 필수입니다")
    private String targetUrl;

    @NotBlank(message = "ServiceKey는 필수입니다")
    private String serviceKey;

    private Map<String, String> params = new HashMap<>();

    private String uddiSeq;
}
