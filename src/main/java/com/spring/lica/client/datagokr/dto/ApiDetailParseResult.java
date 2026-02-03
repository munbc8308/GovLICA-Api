package com.spring.lica.client.datagokr.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ApiDetailParseResult {

    private String publicDataPk;
    private String apiName;
    private String providerOrg;
    private String apiType;
    private String dataFormat;
    private String description;
    private String serviceUrl;
    private String category;

    @Builder.Default
    private List<OperationInfo> operations = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    public static class OperationInfo {
        private String oprtinSeqNo;
        private String operationName;
        private String endpointUrl;
        private String httpMethod;

        @Builder.Default
        private List<ParameterInfo> requestParams = new ArrayList<>();
        @Builder.Default
        private List<ParameterInfo> responseFields = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    public static class ParameterInfo {
        private String nameKor;
        private String nameEng;
        private String size;
        private String division; // 필/옵
        private String sampleData;
        private String description;
    }
}
