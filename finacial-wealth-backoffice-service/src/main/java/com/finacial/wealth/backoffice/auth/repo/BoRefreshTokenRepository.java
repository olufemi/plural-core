package com.finacial.wealth.backoffice.auth.repo;

import com.finacial.wealth.backoffice.auth.entity.BoRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoRefreshTokenRepository extends JpaRepository<BoRefreshToken, Long> {
  Optional<BoRefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
