/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.FinInstitutionType;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author OSHIN
 */
@Repository
public interface FinInstitutionTypeRepository extends
        CrudRepository<FinInstitutionType, String> {

    boolean existsByFinTypeId(String finTypeId);
    

    Optional<FinInstitutionType> findByFinTypeId(String id);

    @Query("SELECT u FROM FinInstitutionType u where u.id = :id")
    Optional<FinInstitutionType> findByFinInstitutionTypeId(@Param("id") String id);

    @Transactional
    @Modifying
    @Query("DELETE FROM FinInstitutionType u where u.id = :id")
    void deleteFinType(@Param("id") String id);

    @Query("select u.institutionTypeName from FinInstitutionType u where u.finTypeId = :finTypeId")
    public String findInstitutionTypeName(@Param("finTypeId") String finTypeId);
}
