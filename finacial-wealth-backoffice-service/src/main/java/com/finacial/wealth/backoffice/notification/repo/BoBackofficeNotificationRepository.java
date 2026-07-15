package com.finacial.wealth.backoffice.notification.repo;

import com.finacial.wealth.backoffice.notification.entity.BoBackofficeNotification;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoBackofficeNotificationRepository extends JpaRepository<BoBackofficeNotification, Long> {

    Page<BoBackofficeNotification> findByRecipientAdminIdOrderByCreatedAtDesc(Long recipientAdminId, Pageable pageable);

    Page<BoBackofficeNotification> findByRecipientAdminIdAndReadAtIsNullOrderByCreatedAtDesc(Long recipientAdminId, Pageable pageable);

    long countByRecipientAdminIdAndReadAtIsNull(Long recipientAdminId);

    Optional<BoBackofficeNotification> findByIdAndRecipientAdminId(Long id, Long recipientAdminId);
}
