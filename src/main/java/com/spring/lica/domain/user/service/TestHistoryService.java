package com.spring.lica.domain.user.service;

import com.spring.lica.common.exception.UserNotFoundException;
import com.spring.lica.domain.catalog.repository.ApiCatalogRepository;
import com.spring.lica.domain.user.dto.TestHistoryResponse;
import com.spring.lica.domain.user.entity.TestHistory;
import com.spring.lica.domain.user.entity.User;
import com.spring.lica.domain.user.repository.TestHistoryRepository;
import com.spring.lica.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestHistoryService {

    private final TestHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ApiCatalogRepository catalogRepository;

    @Transactional(readOnly = true)
    public Page<TestHistoryResponse> getHistory(String email, int page, int size) {
        User user = findUser(email);
        return historyRepository.findByUserIdOrderByExecutedAtDesc(user.getId(), PageRequest.of(page, size))
                .map(TestHistoryResponse::from);
    }

    @Transactional
    public void saveHistory(String email, String requestUrl, String requestParams,
                            String responseBody, Integer responseStatus, String uddiSeq) {
        try {
            var user = userRepository.findByEmail(email).orElse(null);
            if (user == null) return;

            var catalog = (uddiSeq != null)
                    ? catalogRepository.findByUddiSeq(uddiSeq).orElse(null)
                    : null;

            String truncatedBody = responseBody;
            if (truncatedBody != null && truncatedBody.length() > 10000) {
                truncatedBody = truncatedBody.substring(0, 10000) + "\n... [truncated]";
            }

            historyRepository.save(TestHistory.builder()
                    .user(user)
                    .catalog(catalog)
                    .requestUrl(requestUrl)
                    .requestParams(requestParams)
                    .responseBody(truncatedBody)
                    .responseStatus(responseStatus)
                    .build());
        } catch (Exception e) {
            log.error("Failed to save test history", e);
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
    }
}
