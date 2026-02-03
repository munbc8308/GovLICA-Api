package com.spring.lica.domain.catalog.repository;

import com.spring.lica.domain.catalog.entity.ApiOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiOperationRepository extends JpaRepository<ApiOperation, Long> {

    List<ApiOperation> findByCatalogId(Long catalogId);

    void deleteByCatalogId(Long catalogId);
}
