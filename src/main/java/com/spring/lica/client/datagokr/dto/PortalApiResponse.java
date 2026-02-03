package com.spring.lica.client.datagokr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortalApiResponse {

    private Response response;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private int totalCount;
        private int pageNo;
        private int numOfRows;
        private Items items;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<ApiItem> item;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiItem {
        @JsonProperty("uddiSeq")
        private String uddiSeq;

        @JsonProperty("openApiServiceName")
        private String openApiServiceName;

        @JsonProperty("openApiServiceDescription")
        private String openApiServiceDescription;

        @JsonProperty("providerOrgName")
        private String providerOrgName;

        @JsonProperty("classificationName")
        private String classificationName;

        @JsonProperty("serviceType")
        private String serviceType;

        @JsonProperty("dataFormat")
        private String dataFormat;

        @JsonProperty("endpointUrl")
        private String endpointUrl;

        @JsonProperty("lastModifyTime")
        private String lastModifyTime;

        // Alternate field names used by some API versions
        @JsonProperty("서비스명")
        private String serviceName;

        @JsonProperty("서비스설명")
        private String serviceDescription;

        @JsonProperty("제공기관명")
        private String orgName;

        @JsonProperty("분류체계명")
        private String categoryName;

        @JsonProperty("서비스유형")
        private String svcType;

        @JsonProperty("데이터포맷")
        private String dataFmt;

        @JsonProperty("서비스URL")
        private String serviceUrl;

        public String resolvedName() {
            return openApiServiceName != null ? openApiServiceName : serviceName;
        }

        public String resolvedDescription() {
            return openApiServiceDescription != null ? openApiServiceDescription : serviceDescription;
        }

        public String resolvedOrg() {
            return providerOrgName != null ? providerOrgName : orgName;
        }

        public String resolvedCategory() {
            return classificationName != null ? classificationName : categoryName;
        }

        public String resolvedServiceType() {
            return serviceType != null ? serviceType : svcType;
        }

        public String resolvedDataFormat() {
            return dataFormat != null ? dataFormat : dataFmt;
        }

        public String resolvedEndpointUrl() {
            return endpointUrl != null ? endpointUrl : serviceUrl;
        }
    }
}
