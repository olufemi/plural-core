package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.Otp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


public interface OtpRepository  extends JpaRepository<Otp, Long>, JpaSpecificationExecutor<Otp> {
	
	@Query("select ot from Otp ot where ot.otp=:otp AND ot.requestId=:requestId")
	Optional<Otp> findByOtpAndRequestId(String otp, String requestId);
	
	@Query("select ot from Otp ot where ot.requestId=:requestId")
	Optional<Otp> findByRequestId(String requestId);
        
        @Query("select ot from Otp ot where ot.requestId=:requestId")
	List<Otp> findByReqId(String requestId);
	
}
