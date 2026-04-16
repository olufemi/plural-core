package com.finacial.wealth.backoffice.auth.repo;

import com.finacial.wealth.backoffice.auth.entity.BoPermission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoPermissionRepository extends JpaRepository<BoPermission, Long> {

    Optional<BoPermission> findByCode(String code);

    List<BoPermission> findByCodeIn(Collection<String> codes);

    List<BoPermission> findAllByOrderByModuleAscSubModuleAscActionAsc();
}
