/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.service.profiling.bo;

/**
 *
 * @author olufemioshin
 */

import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class RegWalletInfoSpecification {

    private RegWalletInfoSpecification() {
    }

    public static Specification<RegWalletInfo> filter(
            String keyword,
            String customerId,
            String email,
            String phoneNumber,
            String accountNumber,
            String isUserBlocked
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("firstName")), likeValue),
                                cb.like(cb.lower(root.get("lastName")), likeValue),
                                cb.like(cb.lower(root.get("middleName")), likeValue),
                                cb.like(cb.lower(root.get("fullName")), likeValue),
                                cb.like(cb.lower(root.get("email")), likeValue),
                                cb.like(cb.lower(root.get("phoneNumber")), likeValue),
                                cb.like(cb.lower(root.get("customerId")), likeValue),
                                cb.like(cb.lower(root.get("accountNumber")), likeValue),
                                cb.like(cb.lower(root.get("userName")), likeValue),
                                cb.like(cb.lower(root.get("walletId")), likeValue)
                        )
                );
            }

            if (customerId != null && !customerId.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("customerId"), customerId.trim()));
            }

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("email")), email.trim().toLowerCase()));
            }

            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("phoneNumber"), phoneNumber.trim()));
            }

            if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("accountNumber"), accountNumber.trim()));
            }

            if (isUserBlocked != null && !isUserBlocked.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("isUserBlocked"), isUserBlocked.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
