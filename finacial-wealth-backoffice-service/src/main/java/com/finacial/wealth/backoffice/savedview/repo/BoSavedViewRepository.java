package com.finacial.wealth.backoffice.savedview.repo;

import com.finacial.wealth.backoffice.savedview.entity.BoSavedView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoSavedViewRepository extends JpaRepository<BoSavedView, Long> {

    List<BoSavedView> findByAdminUserIdAndModuleKeyOrderByDefaultViewDescUpdatedAtDesc(Long adminUserId, String moduleKey);

    Optional<BoSavedView> findByIdAndAdminUserId(Long id, Long adminUserId);

    List<BoSavedView> findByAdminUserIdAndModuleKeyAndDefaultViewTrue(Long adminUserId, String moduleKey);
}
