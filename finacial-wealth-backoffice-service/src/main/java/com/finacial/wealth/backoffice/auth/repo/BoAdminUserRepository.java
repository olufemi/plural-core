package com.finacial.wealth.backoffice.auth.repo;

import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoAdminUserRepository extends JpaRepository<BoAdminUser, Long>, JpaSpecificationExecutor<BoAdminUser> {

    Optional<BoAdminUser> findByEmailIgnoreCase(String email);

    Optional<BoAdminUser> findByEmail(String email);

    Page<BoAdminUser> findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String email, String fullName, Pageable pageable
    );

    long countByStatus(BoAdminUser.Status status);

}
