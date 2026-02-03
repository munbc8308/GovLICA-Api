package com.spring.lica.client.datagokr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.lica.client.datagokr.dto.ApiDetailParseResult;
import com.spring.lica.client.datagokr.dto.PortalApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DataGoKrClient {

    private static final String SEARCH_URL = "https://www.data.go.kr/tcs/dss/selectDataSetList.do";
    private static final Pattern PK_PATTERN = Pattern.compile("/data/(\\d+)/openapi\\.do");

    private final WebClient webClient;
    private final DataGoKrProperties properties;

    public DataGoKrClient(DataGoKrProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * data.go.kr 검색 페이지를 스크래핑하여 REST 타입 오픈 API 목록을 조회한다.
     * 인증키 불필요.
     */
    public PortalApiResponse searchRestApis(int page, int perPage, String keyword) {
        try {
            Document doc = Jsoup.connect(SEARCH_URL)
                    .data("dType", "API")
                    .data("svcType", "REST")
                    .data("keyword", keyword != null ? keyword : "")
                    .data("currentPage", String.valueOf(page))
                    .data("perPage", String.valueOf(perPage))
                    .data("sort", "updtDt")
                    .timeout(properties.getConnectTimeout() + properties.getReadTimeout())
                    .userAgent("Mozilla/5.0 (compatible; GovLICA/1.0)")
                    .get();

            return parseSearchResults(doc);
        } catch (IOException e) {
            log.error("data.go.kr search page scraping failed", e);
            throw new DataGoKrApiException("공공데이터포털 검색 실패: " + e.getMessage(), e);
        }
    }

    /**
     * data.go.kr 검색 페이지를 스크래핑 (분류 필터 포함)
     */
    public PortalApiResponse searchRestApis(int page, int perPage, String keyword, String category) {
        try {
            var conn = Jsoup.connect(SEARCH_URL)
                    .data("dType", "API")
                    .data("svcType", "REST")
                    .data("keyword", keyword != null ? keyword : "")
                    .data("currentPage", String.valueOf(page))
                    .data("perPage", String.valueOf(perPage))
                    .data("sort", "updtDt")
                    .timeout(properties.getConnectTimeout() + properties.getReadTimeout())
                    .userAgent("Mozilla/5.0 (compatible; GovLICA/1.0)");

            if (category != null && !category.isBlank()) {
                conn.data("brm", category);
            }

            Document doc = conn.get();
            return parseSearchResults(doc);
        } catch (IOException e) {
            log.error("data.go.kr search page scraping failed", e);
            throw new DataGoKrApiException("공공데이터포털 검색 실패: " + e.getMessage(), e);
        }
    }

    private PortalApiResponse parseSearchResults(Document doc) {
        List<PortalApiResponse.ApiItem> items = new ArrayList<>();

        // 총 건수 파싱: "검색결과 7,089건" 같은 텍스트에서 추출
        int totalCount = 0;
        Element totalEl = doc.selectFirst(".result-count, .search-result-count, .result_num");
        if (totalEl != null) {
            String text = totalEl.text().replaceAll("[^0-9]", "");
            if (!text.isEmpty()) totalCount = Integer.parseInt(text);
        }
        // 대안: 탭의 건수에서 추출
        if (totalCount == 0) {
            for (Element tab : doc.select(".tab, .tabmenu a, [data-dtype]")) {
                String tabText = tab.text();
                if (tabText.contains("오픈API") || tabText.contains("API")) {
                    String num = tabText.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) {
                        totalCount = Integer.parseInt(num);
                        break;
                    }
                }
            }
        }
        // 대안: 페이지 전체 텍스트에서 "총 N건" 패턴
        if (totalCount == 0) {
            Matcher m = Pattern.compile("(\\d[\\d,]*)\\s*건").matcher(doc.text());
            while (m.find()) {
                int n = Integer.parseInt(m.group(1).replace(",", ""));
                if (n > totalCount) totalCount = n;
            }
        }

        // 결과 목록 파싱 - data.go.kr의 결과 리스트에서 각 항목 추출
        Elements resultItems = doc.select(".result-list li, .dataset-list li, .data-list li, #apiDataList li, .resultList li");

        // 폴백: 일반 li에서 openapi.do 링크가 있는 항목
        if (resultItems.isEmpty()) {
            resultItems = doc.select("li:has(a[href*=openapi.do])");
        }

        for (Element item : resultItems) {
            PortalApiResponse.ApiItem apiItem = new PortalApiResponse.ApiItem();

            // 링크에서 publicDataPk 추출
            Element link = item.selectFirst("a[href*=openapi.do], a[href*=publicDataPk]");
            if (link != null) {
                String href = link.attr("href");
                Matcher pkMatcher = PK_PATTERN.matcher(href);
                if (pkMatcher.find()) {
                    apiItem.setUddiSeq(pkMatcher.group(1));
                }
                // 제목
                String title = link.text().trim();
                if (!title.isEmpty()) {
                    apiItem.setOpenApiServiceName(title);
                }
            }

            // 제목 (h3에서도 시도)
            if (apiItem.getOpenApiServiceName() == null || apiItem.getOpenApiServiceName().isEmpty()) {
                Element titleEl = item.selectFirst("h3, h4, .title, .data-title");
                if (titleEl != null) {
                    apiItem.setOpenApiServiceName(titleEl.text().trim());
                }
            }

            // uddiSeq가 없으면 스킵
            if (apiItem.getUddiSeq() == null) continue;

            // 설명
            Element descEl = item.selectFirst(".publicDataDesc, .desc, .data-desc, p");
            if (descEl != null) {
                apiItem.setOpenApiServiceDescription(descEl.text().trim());
            }

            // 메타 정보 파싱: 텍스트에서 패턴 매칭
            String itemText = item.text();

            // 제공기관
            Element orgEl = item.selectFirst(".org, .agency");
            if (orgEl != null) {
                apiItem.setProviderOrgName(orgEl.text().trim());
            } else {
                extractMetaField(itemText, "제공기관", apiItem::setProviderOrgName);
            }

            // 분류
            Element brmEl = item.selectFirst(".brm, .category");
            if (brmEl != null) {
                apiItem.setClassificationName(brmEl.text().trim());
            }

            // 데이터포맷: JSON, XML 등
            if (itemText.contains("JSON") && itemText.contains("XML")) {
                apiItem.setDataFormat("JSON,XML");
            } else if (itemText.contains("JSON")) {
                apiItem.setDataFormat("JSON");
            } else if (itemText.contains("XML")) {
                apiItem.setDataFormat("XML");
            }

            apiItem.setServiceType("REST");

            items.add(apiItem);
        }

        log.info("Scraped {} items from data.go.kr (totalCount={})", items.size(), totalCount);

        // 응답 구성
        PortalApiResponse response = new PortalApiResponse();
        PortalApiResponse.Response resp = new PortalApiResponse.Response();
        PortalApiResponse.Header header = new PortalApiResponse.Header();
        header.setResultCode("0000");
        header.setResultMsg("OK");
        resp.setHeader(header);

        PortalApiResponse.Body body = new PortalApiResponse.Body();
        body.setTotalCount(totalCount);
        body.setPageNo(1);
        body.setNumOfRows(items.size());
        PortalApiResponse.Items respItems = new PortalApiResponse.Items();
        respItems.setItem(items);
        body.setItems(respItems);
        resp.setBody(body);
        response.setResponse(resp);

        return response;
    }

    private void extractMetaField(String text, String label, java.util.function.Consumer<String> setter) {
        int idx = text.indexOf(label);
        if (idx >= 0) {
            String sub = text.substring(idx + label.length()).trim();
            // 첫 단어(공백이나 특수문자 전까지)
            String[] parts = sub.split("[\\s|·,]", 2);
            if (parts.length > 0 && !parts[0].isEmpty()) {
                setter.accept(parts[0].trim());
            }
        }
    }

    // ===== Phase 2: API 상세정보 스크래핑 =====

    private static final String DETAIL_URL = "https://www.data.go.kr/data/%s/openapi.do";
    private static final String DETAIL_FUNCTION_URL = "https://www.data.go.kr/tcs/dss/selectApiDetailFunction.do";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Swagger JSON 추출 정규식 (backtick 또는 single quote 모두 지원)
    private static final Pattern SWAGGER_JSON_PATTERN = Pattern.compile("var\\s+swaggerJson\\s*=\\s*[`']([^`']+)[`']");

    // Legacy: JavaScript 파라미터 추출용 정규식
    private static final Pattern JS_PARAM_BLOCK = Pattern.compile("var\\s+paramObj\\s*=\\s*\\{\\}\\s*;(.*?)paramList\\.push\\(paramObj\\)\\s*;", Pattern.DOTALL);
    private static final Pattern JS_PROP_PATTERN = Pattern.compile("paramObj\\.(\\w+)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern JS_OPRTIN_SEQ = Pattern.compile("oprtinSeqNo\\s*=\\s*\"(\\d+)\"");
    private static final Pattern JS_OPRTIN_URL = Pattern.compile("oprtinUrl\\s*=\\s*\"([^\"]+)\"");

    /**
     * data.go.kr API 상세 페이지를 스크래핑하여 오퍼레이션, 요청변수, 응답필드를 파싱한다.
     * 1순위: 페이지에 내장된 Swagger JSON에서 파싱 (최신 페이지)
     * 2순위: JavaScript paramList + AJAX 호출로 파싱 (구형 페이지)
     */
    public ApiDetailParseResult fetchApiDetail(String publicDataPk) {
        try {
            String url = String.format(DETAIL_URL, publicDataPk);
            Connection.Response response = Jsoup.connect(url)
                    .timeout(properties.getConnectTimeout() + properties.getReadTimeout())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .execute();

            Document doc = response.parse();
            String html = doc.html();

            // 1순위: Swagger JSON 파싱 시도
            ApiDetailParseResult swaggerResult = tryParseSwaggerJson(html, publicDataPk);
            if (swaggerResult != null) {
                log.info("Parsed API detail from Swagger JSON for pk={}", publicDataPk);
                // Swagger에 없는 메타 정보 보완
                enrichFromPageMeta(doc, swaggerResult);
                return swaggerResult;
            }

            // 2순위: Legacy 방식 (paramList + AJAX)
            log.info("No Swagger JSON found, falling back to legacy parsing for pk={}", publicDataPk);
            return parseLegacyDetailPage(doc, html, publicDataPk, response.cookies());

        } catch (IOException e) {
            log.error("data.go.kr detail page scraping failed for pk={}", publicDataPk, e);
            throw new DataGoKrApiException("API 상세정보 조회 실패: " + e.getMessage(), e);
        }
    }

    // ========== Swagger JSON 파싱 (1순위) ==========

    private ApiDetailParseResult tryParseSwaggerJson(String html, String publicDataPk) {
        Matcher m = SWAGGER_JSON_PATTERN.matcher(html);
        if (!m.find()) return null;

        String jsonStr = m.group(1).trim();
        if (jsonStr.isEmpty()) return null;

        try {
            JsonNode root = objectMapper.readTree(jsonStr);
            return parseSwaggerSpec(root, publicDataPk);
        } catch (Exception e) {
            log.warn("Failed to parse Swagger JSON for pk={}: {}", publicDataPk, e.getMessage());
            return null;
        }
    }

    private ApiDetailParseResult parseSwaggerSpec(JsonNode root, String publicDataPk) {
        var builder = ApiDetailParseResult.builder().publicDataPk(publicDataPk);

        // info
        JsonNode info = root.path("info");
        builder.apiName(textOrNull(info, "title"));
        builder.description(textOrNull(info, "description"));
        builder.apiType("REST");

        // 1순위: swaggerOprtinVOs (data.go.kr 전용, 기본값·실제 URL 포함)
        JsonNode oprtinVOs = root.path("swaggerOprtinVOs");
        if (oprtinVOs.isArray() && !oprtinVOs.isEmpty()) {
            log.debug("Parsing from swaggerOprtinVOs ({} operations)", oprtinVOs.size());
            parseFromOprtinVOs(root, oprtinVOs, builder);
            return builder.build();
        }

        // 2순위: 표준 Swagger paths (path-level 파라미터 포함)
        log.debug("No swaggerOprtinVOs, falling back to standard Swagger paths");
        parseFromSwaggerPaths(root, builder);
        return builder.build();
    }

    // ========== swaggerOprtinVOs 파싱 (data.go.kr 전용, 가장 풍부한 데이터) ==========

    private void parseFromOprtinVOs(JsonNode root, JsonNode oprtinVOs,
                                     ApiDetailParseResult.ApiDetailParseResultBuilder builder) {
        List<ApiDetailParseResult.OperationInfo> operations = new ArrayList<>();

        // paths에서 HTTP 메서드, produces 추출
        JsonNode paths = root.path("paths");
        java.util.Map<String, String> operationIdToMethod = new java.util.HashMap<>();
        if (paths.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> pathIter = paths.fields();
            while (pathIter.hasNext()) {
                JsonNode methods = pathIter.next().getValue();
                Iterator<Map.Entry<String, JsonNode>> methodIter = methods.fields();
                while (methodIter.hasNext()) {
                    Map.Entry<String, JsonNode> me = methodIter.next();
                    String key = me.getKey().toUpperCase();
                    if ("PARAMETERS".equals(key)) continue;
                    String opId = textOrNull(me.getValue(), "operationId");
                    if (opId != null) operationIdToMethod.put(opId, key);

                    // dataFormat (produces)
                    JsonNode produces = me.getValue().path("produces");
                    if (produces.isArray()) {
                        List<String> formats = new ArrayList<>();
                        for (JsonNode p : produces) {
                            String v = p.asText();
                            if (v.contains("json")) formats.add("JSON");
                            if (v.contains("xml")) formats.add("XML");
                        }
                        if (!formats.isEmpty()) builder.dataFormat(String.join("+", formats));
                    }
                }
            }
        }

        for (JsonNode vo : oprtinVOs) {
            String opUrl = textOrNull(vo, "oprtinUrl");
            String opName = textOrNull(vo, "oprtinNm");
            String operationId = textOrNull(vo, "operationId");
            String httpMethod = operationIdToMethod.getOrDefault(operationId, "GET");

            var opBuilder = ApiDetailParseResult.OperationInfo.builder()
                    .operationName(opName != null ? opName : operationId)
                    .httpMethod(httpMethod)
                    .endpointUrl(opUrl);

            // reqList → requestParams
            List<ApiDetailParseResult.ParameterInfo> reqParams = new ArrayList<>();
            JsonNode reqList = vo.path("reqList");
            if (reqList.isArray()) {
                for (JsonNode req : reqList) {
                    String defaultVal = textOrNull(req, "paramtrBassValue");
                    if ("-".equals(defaultVal)) defaultVal = null;

                    reqParams.add(ApiDetailParseResult.ParameterInfo.builder()
                            .nameEng(textOrNull(req, "paramtrNm"))
                            .description(textOrNull(req, "paramtrDc"))
                            .size(textOrNull(req, "paramtrTy"))
                            .division(textOrNull(req, "paramtrDivision"))
                            .sampleData(defaultVal)
                            .build());
                }
            }
            opBuilder.requestParams(reqParams);

            // resList → responseFields (중첩 subParam 평탄화)
            List<ApiDetailParseResult.ParameterInfo> resFields = new ArrayList<>();
            JsonNode resList = vo.path("resList");
            if (resList.isArray()) {
                flattenResListFields(resList, resFields, "");
            }
            opBuilder.responseFields(resFields);

            operations.add(opBuilder.build());
        }

        // serviceUrl: 첫 번째 오퍼레이션의 URL 사용
        if (!operations.isEmpty() && operations.get(0).getEndpointUrl() != null) {
            builder.serviceUrl(operations.get(0).getEndpointUrl());
        }

        builder.operations(operations);
    }

    /**
     * data.go.kr resList의 중첩 subParam을 평탄화하여 응답 필드 리스트로 변환한다.
     */
    private void flattenResListFields(JsonNode resList, List<ApiDetailParseResult.ParameterInfo> fields, String prefix) {
        for (JsonNode res : resList) {
            String name = textOrNull(res, "paramtrNm");
            if (name == null) continue;
            String fullName = prefix.isEmpty() ? name : prefix + "." + name;
            String type = textOrNull(res, "paramtrTy");
            String desc = textOrNull(res, "paramtrDc");

            fields.add(ApiDetailParseResult.ParameterInfo.builder()
                    .nameEng(fullName)
                    .description(desc)
                    .size(type)
                    .build());

            JsonNode subParam = res.path("subParam");
            if (subParam.isArray() && !subParam.isEmpty()) {
                flattenResListFields(subParam, fields, fullName);
            }
        }
    }

    // ========== 표준 Swagger paths 파싱 (fallback) ==========

    private void parseFromSwaggerPaths(JsonNode root,
                                        ApiDetailParseResult.ApiDetailParseResultBuilder builder) {
        // host + basePath → serviceUrl
        String host = textOrNull(root, "host");
        String basePath = textOrNull(root, "basePath");
        String scheme = "https";
        JsonNode schemes = root.path("schemes");
        if (schemes.isArray() && !schemes.isEmpty()) {
            scheme = schemes.get(0).asText("https");
        }
        String baseUrl = (host != null) ? scheme + "://" + host + (basePath != null ? basePath : "") : null;
        builder.serviceUrl(baseUrl);

        JsonNode definitions = root.path("definitions");

        List<ApiDetailParseResult.OperationInfo> operations = new ArrayList<>();
        JsonNode paths = root.path("paths");
        if (paths.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> pathIter = paths.fields();
            while (pathIter.hasNext()) {
                Map.Entry<String, JsonNode> pathEntry = pathIter.next();
                String pathUrl = pathEntry.getKey();
                JsonNode pathNode = pathEntry.getValue();

                // path-level 파라미터 수집
                List<JsonNode> pathLevelParams = new ArrayList<>();
                JsonNode pathParams = pathNode.path("parameters");
                if (pathParams.isArray()) {
                    for (JsonNode pp : pathParams) pathLevelParams.add(pp);
                }

                Iterator<Map.Entry<String, JsonNode>> methodIter = pathNode.fields();
                while (methodIter.hasNext()) {
                    Map.Entry<String, JsonNode> methodEntry = methodIter.next();
                    String httpMethod = methodEntry.getKey().toUpperCase();
                    if ("PARAMETERS".equals(httpMethod)) continue;
                    JsonNode opNode = methodEntry.getValue();

                    String endpointUrl = baseUrl != null ? baseUrl + pathUrl : pathUrl;
                    String summary = textOrNull(opNode, "summary");
                    String operationId = textOrNull(opNode, "operationId");

                    var opBuilder = ApiDetailParseResult.OperationInfo.builder()
                            .operationName(summary != null ? summary : (operationId != null ? operationId : pathUrl))
                            .httpMethod(httpMethod)
                            .endpointUrl(endpointUrl);

                    // 파라미터: operation-level + path-level 병합
                    List<ApiDetailParseResult.ParameterInfo> reqParams = new ArrayList<>();
                    List<JsonNode> allParams = new ArrayList<>(pathLevelParams);
                    JsonNode opParams = opNode.path("parameters");
                    if (opParams.isArray()) {
                        for (JsonNode op : opParams) allParams.add(op);
                    }

                    for (JsonNode p : allParams) {
                        String paramType = textOrNull(p, "type");
                        if (paramType == null && p.has("schema")) {
                            paramType = textOrNull(p.path("schema"), "type");
                        }
                        String sampleData = textOrNull(p, "example");
                        if (sampleData == null) sampleData = textOrNull(p, "default");

                        reqParams.add(ApiDetailParseResult.ParameterInfo.builder()
                                .nameEng(textOrNull(p, "name"))
                                .description(textOrNull(p, "description"))
                                .size(paramType)
                                .division(p.path("required").asBoolean(false) ? "필수" : "옵션")
                                .sampleData(sampleData)
                                .build());
                    }
                    opBuilder.requestParams(reqParams);

                    // responses → responseFields
                    List<ApiDetailParseResult.ParameterInfo> resFields = new ArrayList<>();
                    JsonNode resp200 = opNode.path("responses").path("200").path("schema");
                    if (!resp200.isMissingNode()) {
                        JsonNode resolved = resolveRef(resp200, definitions);
                        extractResponseFields(resolved, resFields, "", definitions);
                    }
                    opBuilder.responseFields(resFields);

                    operations.add(opBuilder.build());

                    // dataFormat
                    JsonNode produces = opNode.path("produces");
                    if (produces.isArray()) {
                        List<String> formats = new ArrayList<>();
                        for (JsonNode p : produces) {
                            String v = p.asText();
                            if (v.contains("json")) formats.add("JSON");
                            if (v.contains("xml")) formats.add("XML");
                        }
                        if (!formats.isEmpty()) builder.dataFormat(String.join("+", formats));
                    }
                }
            }
        }

        builder.operations(operations);
    }

    /**
     * Swagger $ref 참조를 해석하여 실제 스키마 노드를 반환한다.
     */
    private JsonNode resolveRef(JsonNode node, JsonNode definitions) {
        if (node.has("$ref")) {
            String ref = node.get("$ref").asText();
            if (ref.startsWith("#/definitions/") && definitions.isObject()) {
                String modelName = ref.substring("#/definitions/".length());
                JsonNode resolved = definitions.path(modelName);
                if (!resolved.isMissingNode()) return resolved;
            }
        }
        return node;
    }

    /**
     * Swagger response schema에서 응답 필드를 재귀적으로 추출한다.
     */
    private void extractResponseFields(JsonNode schema, List<ApiDetailParseResult.ParameterInfo> fields,
                                        String prefix, JsonNode definitions) {
        JsonNode resolved = resolveRef(schema, definitions);

        String type = textOrNull(resolved, "type");
        if ("array".equals(type) && resolved.has("items")) {
            JsonNode items = resolveRef(resolved.path("items"), definitions);
            extractResponseFields(items, fields, prefix, definitions);
            return;
        }

        JsonNode properties = resolved.path("properties");
        if (!properties.isObject()) return;

        Iterator<Map.Entry<String, JsonNode>> iter = properties.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            String fieldName = entry.getKey();
            JsonNode fieldNode = resolveRef(entry.getValue(), definitions);
            String fieldType = textOrNull(fieldNode, "type");
            String desc = textOrNull(fieldNode, "description");
            String fullName = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            if ("object".equals(fieldType) && fieldNode.has("properties")) {
                extractResponseFields(fieldNode, fields, fullName, definitions);
            } else if ("array".equals(fieldType) && fieldNode.has("items")) {
                JsonNode items = resolveRef(fieldNode.path("items"), definitions);
                fields.add(ApiDetailParseResult.ParameterInfo.builder()
                        .nameEng(fullName).description(desc).size("array").build());
                extractResponseFields(items, fields, fullName, definitions);
            } else {
                fields.add(ApiDetailParseResult.ParameterInfo.builder()
                        .nameEng(fullName).description(desc).size(fieldType).build());
            }
        }
    }

    private void enrichFromPageMeta(Document doc, ApiDetailParseResult result) {
        String fullText = doc.text();
        if (result.getProviderOrg() == null) {
            extractMetaField(fullText, "제공기관", result::setProviderOrg);
        }
        if (result.getCategory() == null) {
            extractMetaField(fullText, "분류체계", result::setCategory);
        }
        // dt/dd에서 추출
        for (Element dt : doc.select("dt, th")) {
            String label = dt.text().trim();
            Element value = dt.nextElementSibling();
            if (value == null) continue;
            String val = value.text().trim();
            if (val.isEmpty()) continue;
            if ("제공기관".equals(label) && result.getProviderOrg() == null) result.setProviderOrg(val);
            if ("분류체계".equals(label) && result.getCategory() == null) result.setCategory(val);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.path(field);
        if (child.isMissingNode() || child.isNull()) return null;
        String text = child.asText().trim();
        return text.isEmpty() ? null : text;
    }

    // ========== Legacy 파싱 (2순위: paramList + AJAX) ==========

    private ApiDetailParseResult parseLegacyDetailPage(Document doc, String html, String publicDataPk, Map<String, String> cookies) {
        var builder = ApiDetailParseResult.builder().publicDataPk(publicDataPk);
        String fullText = doc.text();

        // --- API 이름 ---
        Element titleEl = doc.selectFirst("h3.tit, .tit, h2");
        if (titleEl != null) {
            String title = titleEl.text().trim();
            if (title.contains("|")) title = title.substring(0, title.indexOf("|")).trim();
            builder.apiName(title);
        }
        if (titleEl == null || titleEl.text().isBlank()) {
            String pageTitle = doc.title();
            if (pageTitle != null && !pageTitle.isBlank()) {
                if (pageTitle.contains("|")) pageTitle = pageTitle.substring(0, pageTitle.indexOf("|")).trim();
                if (pageTitle.contains("-")) pageTitle = pageTitle.substring(0, pageTitle.indexOf("-")).trim();
                builder.apiName(pageTitle.trim());
            }
        }

        // --- 메타 정보 ---
        builder.apiType("REST");
        builder.dataFormat(extractFormatInfo(fullText));
        extractMetaFromPage(doc, fullText, builder);

        // --- 설명 ---
        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null && !metaDesc.attr("content").isBlank()) {
            builder.description(metaDesc.attr("content").trim());
        }

        // --- 서비스 URL ---
        String serviceUrl = extractServiceUrl(html, fullText, doc);
        builder.serviceUrl(serviceUrl);

        // --- oprtinSeqNo ---
        String oprtinSeqNo = extractJsVariable(html, JS_OPRTIN_SEQ);

        // --- 오퍼레이션 목록 ---
        List<ApiDetailParseResult.OperationInfo> operations = parseLegacyOperations(doc, html, serviceUrl, oprtinSeqNo);

        // --- JavaScript paramList에서 요청 파라미터 ---
        List<ApiDetailParseResult.ParameterInfo> requestParams = parseJsParamList(html);
        if (!operations.isEmpty() && !requestParams.isEmpty()) {
            operations.get(0).getRequestParams().addAll(requestParams);
        }

        // --- AJAX로 응답 필드 ---
        fetchResponseFieldsFromAjax(operations, publicDataPk, cookies);

        builder.operations(operations);
        return builder.build();
    }

    private List<ApiDetailParseResult.ParameterInfo> parseJsParamList(String html) {
        List<ApiDetailParseResult.ParameterInfo> params = new ArrayList<>();
        Matcher blockMatcher = JS_PARAM_BLOCK.matcher(html);

        while (blockMatcher.find()) {
            String block = blockMatcher.group(1);
            Map<String, String> props = new java.util.HashMap<>();
            Matcher propMatcher = JS_PROP_PATTERN.matcher(block);
            while (propMatcher.find()) {
                props.put(propMatcher.group(1), propMatcher.group(2));
            }
            if (props.isEmpty()) continue;

            params.add(ApiDetailParseResult.ParameterInfo.builder()
                    .nameEng(props.getOrDefault("paramtrNm", ""))
                    .nameKor(props.getOrDefault("paramtrKorNm", ""))
                    .description(props.getOrDefault("paramtrDc", ""))
                    .division(props.getOrDefault("paramtrDivision", ""))
                    .sampleData(props.getOrDefault("paramtrBassValue", ""))
                    .size(props.getOrDefault("paramtrSize", ""))
                    .build());
        }
        log.debug("Parsed {} request parameters from JavaScript paramList", params.size());
        return params;
    }

    private void fetchResponseFieldsFromAjax(List<ApiDetailParseResult.OperationInfo> operations,
                                              String publicDataPk, Map<String, String> cookies) {
        for (ApiDetailParseResult.OperationInfo op : operations) {
            String seqNo = op.getOprtinSeqNo();
            if (seqNo == null || seqNo.isBlank()) continue;

            try {
                Document ajaxDoc = Jsoup.connect(DETAIL_FUNCTION_URL)
                        .method(Connection.Method.POST)
                        .data("oprtinSeqNo", seqNo)
                        .data("publicDataPk", publicDataPk)
                        .data("publicDataDetailPk", publicDataPk)
                        .cookies(cookies)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Referer", String.format(DETAIL_URL, publicDataPk))
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(properties.getReadTimeout())
                        .post();

                parseAjaxResponseTables(ajaxDoc, op);
                log.debug("AJAX detail for oprtinSeqNo={}: req={}, res={}",
                        seqNo, op.getRequestParams().size(), op.getResponseFields().size());
            } catch (IOException e) {
                log.warn("Failed to fetch AJAX detail for oprtinSeqNo={}: {}", seqNo, e.getMessage());
            }
        }
    }

    private void parseAjaxResponseTables(Document ajaxDoc, ApiDetailParseResult.OperationInfo operation) {
        Elements tables = ajaxDoc.select("table");
        for (Element table : tables) {
            String context = "";
            Element prev = table.previousElementSibling();
            if (prev != null) context = prev.text();
            Element parent = table.parent();
            if (context.isEmpty() && parent != null) {
                Element parentPrev = parent.previousElementSibling();
                if (parentPrev != null) context = parentPrev.text();
            }
            Element caption = table.selectFirst("caption");
            if (caption != null && context.isEmpty()) context = caption.text();

            boolean isResponse = context.contains("출력") || context.contains("응답")
                    || context.contains("Response") || context.contains("결과");

            Elements rows = table.select("tbody tr, tr:has(td)");
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() < 2) continue;

                var pb = ApiDetailParseResult.ParameterInfo.builder();
                if (cells.size() > 0) pb.nameKor(cells.get(0).text().trim());
                if (cells.size() > 1) pb.nameEng(cells.get(1).text().trim());
                if (cells.size() > 2) pb.size(cells.get(2).text().trim());
                if (cells.size() > 3) pb.division(cells.get(3).text().trim());
                if (cells.size() > 4) pb.sampleData(cells.get(4).text().trim());
                if (cells.size() > 5) pb.description(cells.get(5).text().trim());

                ApiDetailParseResult.ParameterInfo param = pb.build();
                if ((param.getNameEng() == null || param.getNameEng().isBlank())
                        && (param.getNameKor() == null || param.getNameKor().isBlank())) continue;

                if (isResponse) {
                    operation.getResponseFields().add(param);
                } else {
                    boolean exists = operation.getRequestParams().stream()
                            .anyMatch(p -> p.getNameEng() != null && p.getNameEng().equals(param.getNameEng()));
                    if (!exists) operation.getRequestParams().add(param);
                }
            }
        }
    }

    private List<ApiDetailParseResult.OperationInfo> parseLegacyOperations(Document doc, String html,
                                                                            String serviceUrl, String defaultOprtinSeqNo) {
        List<ApiDetailParseResult.OperationInfo> operations = new ArrayList<>();

        Elements options = doc.select("#open_api_detail_select option, select[name*=oprtin] option");
        if (!options.isEmpty()) {
            for (Element option : options) {
                String val = option.val();
                if (val == null || val.isBlank()) continue;
                operations.add(ApiDetailParseResult.OperationInfo.builder()
                        .oprtinSeqNo(val)
                        .operationName(option.text().trim())
                        .httpMethod("GET")
                        .endpointUrl(serviceUrl)
                        .build());
            }
        }

        if (operations.isEmpty()) {
            String opName = null;
            Element opEl = doc.selectFirst("h3.tit, .tit, h2");
            if (opEl != null) opName = opEl.text().trim();
            operations.add(ApiDetailParseResult.OperationInfo.builder()
                    .oprtinSeqNo(defaultOprtinSeqNo)
                    .operationName(opName != null ? opName : "Default Operation")
                    .httpMethod("GET")
                    .endpointUrl(serviceUrl)
                    .build());
        }
        return operations;
    }

    private String extractJsVariable(String html, Pattern pattern) {
        Matcher m = pattern.matcher(html);
        return m.find() ? m.group(1) : null;
    }

    // ========== 공통 유틸 ==========

    private void extractMetaFromPage(Document doc, String fullText, ApiDetailParseResult.ApiDetailParseResultBuilder builder) {
        for (Element dt : doc.select("dt, th")) {
            String label = dt.text().trim();
            Element value = dt.nextElementSibling();
            if (value == null) continue;
            String val = value.text().trim();
            switch (label) {
                case "제공기관" -> builder.providerOrg(val);
                case "분류체계" -> builder.category(val);
                case "API유형" -> builder.apiType(val);
                case "데이터포맷" -> builder.dataFormat(val);
            }
        }
        if (fullText.contains("제공기관")) {
            extractMetaField(fullText, "제공기관", builder::providerOrg);
        }
    }

    private String extractServiceUrl(String html, String fullText, Document doc) {
        String jsUrl = extractJsVariable(html, JS_OPRTIN_URL);
        if (jsUrl != null && !jsUrl.isBlank()) return jsUrl;

        Matcher urlMatcher = Pattern.compile("(?:서비스URL|요청주소|EndPoint)[\\s:]*?(https?://[^\\s\"'<>]+)").matcher(fullText);
        if (urlMatcher.find()) return urlMatcher.group(1).trim();

        Element urlEl = doc.selectFirst("input[name=serviceUrl], input[name=endpointUrl]");
        if (urlEl != null && !urlEl.val().isBlank()) return urlEl.val();

        Matcher apisMatcher = Pattern.compile("(https?://apis\\.data\\.go\\.kr[^\\s\"'<>]+)").matcher(html);
        if (apisMatcher.find()) return apisMatcher.group(1).trim();

        return null;
    }

    private String extractFormatInfo(String text) {
        boolean json = text.contains("JSON");
        boolean xml = text.contains("XML");
        if (json && xml) return "JSON+XML";
        if (json) return "JSON";
        if (xml) return "XML";
        return null;
    }

    private String extractBetween(String text, String label, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(label) && text.contains(candidate)) return candidate;
        }
        return null;
    }

    /**
     * 임의의 공공데이터 REST API를 프록시로 호출 (테스트 콘솔용)
     */
    public String proxyCall(String targetUrl, Map<String, String> params) {
        try {
            return WebClient.create()
                    .get()
                    .uri(uriBuilder -> {
                        uriBuilder.scheme("https");
                        String cleanUrl = targetUrl.replaceFirst("^https?://", "");
                        int pathIdx = cleanUrl.indexOf('/');
                        if (pathIdx > 0) {
                            uriBuilder.host(cleanUrl.substring(0, pathIdx));
                            uriBuilder.path(cleanUrl.substring(pathIdx));
                        } else {
                            uriBuilder.host(cleanUrl);
                        }
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(properties.getReadTimeout()))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Proxy call HTTP error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new DataGoKrApiException("API 프록시 호출 실패: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Proxy call failed: url={}", targetUrl, e);
            throw new DataGoKrApiException("API 프록시 호출 실패", e);
        }
    }
}
