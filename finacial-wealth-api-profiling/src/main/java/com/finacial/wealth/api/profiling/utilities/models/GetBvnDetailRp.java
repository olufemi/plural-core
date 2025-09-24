/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import lombok.Data;

/**
 *
 * @author gol
 */
@Data
public class GetBvnDetailRp {

    private String bvn;
    private String base64Image;

    private String firstName;
    private String middleName;
    private String lastName;
    private String phoneNumber;
    private String phoneNumber2;
    private String dateOfBirth;
    private String registrationDate;
    private String email;
    private String gender;
    private String lgaOfOrigin;
    private String lgaOfResidence;
    private String maritalStatus;
    private String nationality;
    private String residentialAddress;
    private String stateOfOrigin;
    private String stateOfResidence;

    public GetBvnDetailRp() {
    }

    public GetBvnDetailRp(BvnResponseModel b) {
        this.bvn = b.getBVN();
        // this.base64Image = b.getBase64Image();
        this.firstName = b.getFirstName();
        this.lastName = b.getLastName();
        this.middleName = b.getMiddleName();
        this.phoneNumber = b.getPhoneNumber1();
        this.phoneNumber2 = b.getPhoneNumber2();
        this.lgaOfOrigin = b.getLgaOfOrigin();
        this.lgaOfResidence = b.getLgaOfResidence();
        this.maritalStatus = b.getMaritalStatus();
        this.nationality = b.getNationality();
        this.registrationDate = b.getRegistrationDate();
        this.stateOfOrigin = b.getStateOfOrigin();
        this.stateOfResidence = b.getStateOfResidence();
        this.gender = b.getGender();
        this.email = b.getEmail();
        this.residentialAddress = b.getResidentialAddress();
        this.dateOfBirth = b.getDateOfBirth();
    }

}
