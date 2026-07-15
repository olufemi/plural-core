package com.finacial.wealth.backoffice.configmanagement.repo;

import com.finacial.wealth.backoffice.configmanagement.entity.AppConfigEntry;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigEntryRepository extends JpaRepository<AppConfigEntry, Long> {

    Optional<AppConfigEntry> findFirstByConfigNameIgnoreCase(String configName);

    Page<AppConfigEntry> findByConfigNameContainingIgnoreCaseOrConfigDescriptionContainingIgnoreCase(
            String configName,
            String configDescription,
            Pageable pageable);
}
