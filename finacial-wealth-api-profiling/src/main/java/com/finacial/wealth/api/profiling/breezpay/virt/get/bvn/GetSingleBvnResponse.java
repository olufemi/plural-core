/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.bvn;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetSingleBvnResponse {

    private String status;
    private String message;
    private BvnData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BvnData {

        private String nameOnCard;
        private String bvn;
        private String firstName;
        private String middleName;
        private String lastName;
        private String dateOfBirth;       // e.g. "22-Oct-1970"
        private String phoneNumber1;
        private String registrationDate;  // e.g. "16-Nov-2014"
        private String enrollmentBank;
        private String enrollmentBranch;
        private String email;
        private String gender;
        private String phoneNumber2;
        private String levelOfAccount;
        private String lgaOfOrigin;
        private String lgaOfResidence;
        private String maritalStatus;
        private String nin;
        private String nationality;
        private String residentialAddress;
        private String stateOfOrigin;
        private String stateOfResidence;
        private String title;
        private String watchListed;       // "YES"/"NO"
        private String base64Image;       // long string
        private String responseCode;      // "00"
    }
}
