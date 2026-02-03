package com.spring.lica.client.datagokr;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.datagokr")
public class DataGoKrProperties {

    private String baseUrl = "https://apis.data.go.kr";
    private String portalUrl = "https://www.data.go.kr";
    private int connectTimeout = 5000;
    private int readTimeout = 15000;
}
