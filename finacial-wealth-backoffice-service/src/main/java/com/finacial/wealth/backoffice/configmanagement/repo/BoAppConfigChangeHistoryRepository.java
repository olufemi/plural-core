package com.finacial.wealth.backoffice.configmanagement.repo;

import com.finacial.wealth.backoffice.configmanagement.entity.BoAppConfigChangeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoAppConfigChangeHistoryRepository extends JpaRepository<BoAppConfigChangeHistory, Long> {

    Page<BoAppConfigChangeHistory> findByConfigNameIgnoreCaseOrderByCreatedAtDesc(String configName, Pageable pageable);
}
