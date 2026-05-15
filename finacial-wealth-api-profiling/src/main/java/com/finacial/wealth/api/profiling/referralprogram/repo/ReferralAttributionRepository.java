package com.finacial.wealth.api.profiling.referralprogram.repo;

import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralAttribution;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramProductType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralAttributionRepository extends JpaRepository<ReferralAttribution, Long> {

    Optional<ReferralAttribution> findByRefereeWalletIdAndProductType(String refereeWalletId,
            ReferralProgramProductType productType);
}
