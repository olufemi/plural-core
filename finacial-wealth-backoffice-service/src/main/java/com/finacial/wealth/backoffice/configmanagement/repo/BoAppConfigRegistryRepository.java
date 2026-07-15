package com.finacial.wealth.backoffice.configmanagement.repo;

import com.finacial.wealth.backoffice.configmanagement.entity.BoAppConfigRegistry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoAppConfigRegistryRepository extends JpaRepository<BoAppConfigRegistry, Long> {

    Optional<BoAppConfigRegistry> findFirstByConfigNameIgnoreCase(String configName);
}
