package com.spring.lica.domain.proxy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProxyResponse {
    private int status;
    private String body;
    private long elapsedMs;
    private String curl;
}
