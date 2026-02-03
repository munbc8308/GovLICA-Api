package com.spring.lica.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceKeyRequest {

    @NotBlank(message = "키 이름은 필수입니다")
    private String keyName;

    @NotBlank(message = "서비스키는 필수입니다")
    private String serviceKey;
}
