package com.finacial.wealth.api.profiling.market.repo;

import com.finacial.wealth.api.profiling.market.entities.CustomerMarketProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerMarketProfileRepo extends JpaRepository<CustomerMarketProfile, Long> {

    Optional<CustomerMarketProfile> findByCustomerIdAndMarketCode(String customerId, String marketCode);

    List<CustomerMarketProfile> findByCustomerId(String customerId);

    boolean existsByCustomerIdAndMarketCodeAndStatus(String customerId, String marketCode, String status);
}
