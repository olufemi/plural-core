package com.finacial.wealth.api.profiling.referralprogram.repo;

import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgram;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramProductType;
import com.finacial.wealth.api.profiling.referralprogram.ennum.ReferralProgramStatus;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferralProgramRepository extends JpaRepository<ReferralProgram, Long> {

    Optional<ReferralProgram> findByProgramCodeIgnoreCase(String programCode);

    List<ReferralProgram> findByProductTypeOrderByCreatedAtDesc(ReferralProgramProductType productType);

    @Query("SELECT r FROM ReferralProgram r " +
            "WHERE r.productType = :productType AND r.status = :status " +
            "AND (r.startAt IS NULL OR r.startAt <= :now) " +
            "AND (r.endAt IS NULL OR r.endAt >= :now) " +
            "ORDER BY r.createdAt DESC")
    List<ReferralProgram> findActivePrograms(
            @Param("productType") ReferralProgramProductType productType,
            @Param("status") ReferralProgramStatus status,
            @Param("now") Date now);

    @Query("SELECT r FROM ReferralProgram r " +
            "WHERE r.productType = :productType AND r.status = :status AND r.id <> :excludedId")
    List<ReferralProgram> findOtherProgramsByProductAndStatus(
            @Param("productType") ReferralProgramProductType productType,
            @Param("status") ReferralProgramStatus status,
            @Param("excludedId") Long excludedId);
}
