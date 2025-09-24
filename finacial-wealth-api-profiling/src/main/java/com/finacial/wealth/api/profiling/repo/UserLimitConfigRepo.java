/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.repo;
import com.finacial.wealth.api.profiling.domain.UserLimitConfig;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author olufemioshin
 */
public interface UserLimitConfigRepo extends CrudRepository<UserLimitConfig, String> {

	@Query("select config from UserLimitConfig config where config.walletNumber=:walletNumber")
	List<UserLimitConfig> findByWalletNumber(String walletNumber);

	@Query("select bs from UserLimitConfig bs where bs.walletNumber=:walletNumber")
	UserLimitConfig findByWalletNumberQuery(String walletNumber);

	List<UserLimitConfig> findByTierCategory(String tierCategory);

}
