/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models.accounts;

import com.finacial.wealth.api.profiling.repo.AddFailedTransLoggRepo;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class AddAccountObj {

    private String countryCode;
   // private String walletId;
    private String country;
    private String bvn;
   // private String phoneNumber;
    private String requestId;
    private int otp;

}
