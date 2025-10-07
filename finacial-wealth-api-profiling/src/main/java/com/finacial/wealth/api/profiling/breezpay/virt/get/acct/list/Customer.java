/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.acct.list;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public  class Customer {
        private String customer_name;
        private String customer_id;
        private String customer_email;
        private String merchant_id;
        private String virtual_account_no;
        private String virtual_account_currency;
    }
