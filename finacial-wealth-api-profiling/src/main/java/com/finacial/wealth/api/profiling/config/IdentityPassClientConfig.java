/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**

 @author victor.akinola
 */
@Data
@Component
@ConfigurationProperties(prefix = "fell.idpass.api")
public class IdentityPassClientConfig {

    private String baseUrl;
    private String nin;
    private String vnin;
    private String pvc;
    private String bvnWithImage;
    private String bvnBasic;
    private String driversLicense;
    private String nip;
    private String facial;
    private String faceliveliness;
    private String cacBasic;
    private String cacBasicWithName;
    private String cacAdvance;
    private String bankCodes;
    private String tin;
    private String bankaccountVer;
}
