package com.spring.lica.admin;

import com.spring.lica.domain.catalog.repository.ApiCatalogRepository;
import com.spring.lica.domain.user.repository.TestHistoryRepository;
import com.spring.lica.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final ApiCatalogRepository catalogRepository;
    private final TestHistoryRepository historyRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCatalogs", catalogRepository.count());
        stats.put("restCatalogs", catalogRepository.countByServiceType("REST"));
        stats.put("totalTests", historyRepository.count());
        stats.put("testsToday", historyRepository.countSince(LocalDateTime.now().toLocalDate().atStartOfDay()));
        stats.put("testsThisWeek", historyRepository.countSince(LocalDateTime.now().minusDays(7)));
        return stats;
    }
}
