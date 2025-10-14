/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.bvn;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ValidateSingleBvnReq {

    private String appId;
    private String appReference;
    private String bvn;

}
