package com.spring.lica.domain.catalog.repository;

import com.spring.lica.domain.catalog.entity.ApiCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApiCatalogRepository extends JpaRepository<ApiCatalog, Long> {

    Optional<ApiCatalog> findByUddiSeq(String uddiSeq);

    @Query("""
        SELECT DISTINCT c FROM ApiCatalog c
        LEFT JOIN FETCH c.operations o
        LEFT JOIN FETCH o.parameters
        WHERE c.uddiSeq = :uddiSeq
    """)
    Optional<ApiCatalog> findByUddiSeqWithDetail(@Param("uddiSeq") String uddiSeq);

    boolean existsByUddiSeq(String uddiSeq);

    @Query("""
        SELECT c FROM ApiCatalog c
        WHERE c.serviceType = 'REST'
        AND (:keyword IS NULL OR :keyword = ''
            OR LOWER(c.apiName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.providerOrg) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:category IS NULL OR :category = '' OR c.category = :category)
        AND (:org IS NULL OR :org = '' OR c.providerOrg = :org)
        ORDER BY c.updatedAt DESC
    """)
    Page<ApiCatalog> searchRestApis(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("org") String org,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c.category FROM ApiCatalog c WHERE c.category IS NOT NULL AND c.serviceType = 'REST' ORDER BY c.category")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT c.providerOrg FROM ApiCatalog c WHERE c.providerOrg IS NOT NULL AND c.serviceType = 'REST' ORDER BY c.providerOrg")
    List<String> findDistinctProviderOrgs();

    long countByServiceType(String serviceType);
}
