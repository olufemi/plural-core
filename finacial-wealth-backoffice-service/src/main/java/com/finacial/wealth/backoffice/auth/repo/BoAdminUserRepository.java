package com.finacial.wealth.backoffice.auth.repo;

import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoAdminUserRepository extends JpaRepository<BoAdminUser, Long> {
  Optional<BoAdminUser> findByEmailIgnoreCase(String email);
  Optional<BoAdminUser> findByEmail(String email);
   
}
