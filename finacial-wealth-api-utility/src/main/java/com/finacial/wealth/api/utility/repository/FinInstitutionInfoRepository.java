/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;
import com.finacial.wealth.api.utility.domains.FinInstitutionInfo;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author OSHIN
 */
public interface FinInstitutionInfoRepository extends
        CrudRepository<FinInstitutionInfo, String> {

    boolean existsByFinInstitutionId(String finInstitutionId);
    boolean existsByEmailAddress(String emailAddress);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByFinInstitutionName(String finInstitutionName);

    @Query("SELECT u FROM FinInstitutionInfo u where u.id = :id")
    Optional<FinInstitutionInfo> findByFinInstitutionInfoId(@Param("id") String id);

    @Transactional
    @Modifying
    @Query("DELETE FROM FinInstitutionInfo u where u.id = :id")
    void deleteFinInsts(@Param("id") String id);

    @Query("select u.finInstitutionTypeId from FinInstitutionInfo u where u.finInstitutionId = :finInstitutionId")
    public String findFinInstitutionTypeId(@Param("finInstitutionId") String finInstitutionId);
    
    @Query("select u.finInstitutionName from FinInstitutionInfo u where u.finInstitutionId = :finInstitutionId")
    public String findFinInstitutionName(@Param("finInstitutionId") String finInstitutionId);
}
