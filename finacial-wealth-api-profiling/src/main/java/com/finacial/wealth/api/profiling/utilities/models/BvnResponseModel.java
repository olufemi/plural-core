/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author HRH
 */
public class BvnResponseModel {

    @JsonIgnore
    private String ResponseCode;
    @JsonProperty("bvn")
    private String BVN;
    @JsonProperty("firstName")
    private String FirstName;
    @JsonProperty("middleName")
    private String MiddleName;
    @JsonProperty("lastName")
    private String LastName;
    @JsonProperty("dob")
    private String DateOfBirth;
    @JsonIgnore
    private String RegistrationDate;
    @JsonIgnore
    private String EnrollmentBank;
    @JsonIgnore
    private String EnrollmentBranch;
    @JsonProperty("email")
    private String Email;
    @JsonProperty("gender")
    private String Gender;
    @JsonProperty("accountLevel")
    private String LevelOfAccount;
    @JsonProperty("lgaOfOrigin")
    private String LgaOfOrigin;
    @JsonProperty("lgaOfResidence")
    private String LgaOfResidence;
    @JsonProperty("maritalStatus")
    private String MaritalStatus;
    @JsonProperty("nin")
    private String NIN;
    @JsonProperty("nameOnCard")
    private String NameOnCard;
    @JsonProperty("nationality")
    private String Nationality;
    @JsonProperty("phoneNumber")
    private String PhoneNumber1;
    @JsonProperty("phoneNumber2")
    private String PhoneNumber2;
    @JsonProperty("residentialAddress")
    private String ResidentialAddress;
    @JsonProperty("stateOfOrigin")
    private String StateOfOrigin;
    @JsonProperty("stateOfResidence")
    private String StateOfResidence;
    @JsonIgnore
    private String WatchListed;
    @JsonIgnore
    private String Base64Image;

    public BvnResponseModel() {
    }

//    public BvnResponseModel(String dateOfBirth) {
//        this.DateOfBirth = dateOfBirth;
//    }
    public BvnResponseModel(boolean simulate, String bvn, String email, String phoneNumber, 
            String firstName, String lstName, String phn2) {
        this.ResponseCode = "00";
        this.BVN = bvn;
        this.FirstName = firstName;
        this.MiddleName = "MiddleName";
        this.LastName = lstName;
        this.DateOfBirth = "10-Mar-1990";
        this.RegistrationDate = "10-Mar-2020";
        this.EnrollmentBank = "EnrollmentBank";
        this.EnrollmentBranch = "EnrollmentBranch";
        this.Email = email;
        this.Gender = "Male";
        this.LevelOfAccount = "3";
        this.LgaOfOrigin = "Lagos";
        this.LgaOfResidence = "LgaOfResidence";
        this.MaritalStatus = "MaritalStatus";
        this.NIN = bvn;
        this.NameOnCard = "NameOnCard";
        this.Nationality = "Nationality";
        this.PhoneNumber1 = phoneNumber;
        this.PhoneNumber2 = phn2;
        this.ResidentialAddress = "ResidentialAddress";
        this.StateOfOrigin = "StateOfOrigin";
        this.StateOfResidence = "StateOfResidence";
        this.WatchListed = "WatchListed";
        this.Base64Image = "Base64Image";
    }

    // Getter Methods
    public String getResponseCode() {
        return ResponseCode;
    }

    public String getBVN() {
        return BVN;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getMiddleName() {
        return MiddleName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public String getRegistrationDate() {
        return RegistrationDate;
    }

    public String getEnrollmentBank() {
        return EnrollmentBank;
    }

    public String getEnrollmentBranch() {
        return EnrollmentBranch;
    }

    public String getEmail() {
        return Email;
    }

    public String getGender() {
        return Gender;
    }

    public String getLevelOfAccount() {
        return LevelOfAccount;
    }

    public String getLgaOfOrigin() {
        return LgaOfOrigin;
    }

    public String getLgaOfResidence() {
        return LgaOfResidence;
    }

    public String getMaritalStatus() {
        return MaritalStatus;
    }

    public String getNIN() {
        return NIN;
    }

    public String getNameOnCard() {
        return NameOnCard;
    }

    public String getNationality() {
        return Nationality;
    }

    public String getPhoneNumber1() {
        return PhoneNumber1;
    }

    public String getPhoneNumber2() {
        return PhoneNumber2;
    }

    public String getResidentialAddress() {
        return ResidentialAddress;
    }

    public String getStateOfOrigin() {
        return StateOfOrigin;
    }

    public String getStateOfResidence() {
        return StateOfResidence;
    }

    public String getWatchListed() {
        return WatchListed;
    }

    public String getBase64Image() {
        return Base64Image;
    }

    // Setter Methods
    public void setResponseCode(String ResponseCode) {
        this.ResponseCode = ResponseCode;
    }

    public void setBVN(String BVN) {
        this.BVN = BVN;
    }

    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }

    public void setMiddleName(String MiddleName) {
        this.MiddleName = MiddleName;
    }

    public void setLastName(String LastName) {
        this.LastName = LastName;
    }

    public void setDateOfBirth(String DateOfBirth) {
        this.DateOfBirth = DateOfBirth;
    }

    public void setRegistrationDate(String RegistrationDate) {
        this.RegistrationDate = RegistrationDate;
    }

    public void setEnrollmentBank(String EnrollmentBank) {
        this.EnrollmentBank = EnrollmentBank;
    }

    public void setEnrollmentBranch(String EnrollmentBranch) {
        this.EnrollmentBranch = EnrollmentBranch;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public void setLevelOfAccount(String LevelOfAccount) {
        this.LevelOfAccount = LevelOfAccount;
    }

    public void setLgaOfOrigin(String LgaOfOrigin) {
        this.LgaOfOrigin = LgaOfOrigin;
    }

    public void setLgaOfResidence(String LgaOfResidence) {
        this.LgaOfResidence = LgaOfResidence;
    }

    public void setMaritalStatus(String MaritalStatus) {
        this.MaritalStatus = MaritalStatus;
    }

    public void setNIN(String NIN) {
        this.NIN = NIN;
    }

    public void setNameOnCard(String NameOnCard) {
        this.NameOnCard = NameOnCard;
    }

    public void setNationality(String Nationality) {
        this.Nationality = Nationality;
    }

    public void setPhoneNumber1(String PhoneNumber1) {
        this.PhoneNumber1 = PhoneNumber1;
    }

    public void setPhoneNumber2(String PhoneNumber2) {
        this.PhoneNumber2 = PhoneNumber2;
    }

    public void setResidentialAddress(String ResidentialAddress) {
        this.ResidentialAddress = ResidentialAddress;
    }

    public void setStateOfOrigin(String StateOfOrigin) {
        this.StateOfOrigin = StateOfOrigin;
    }

    public void setStateOfResidence(String StateOfResidence) {
        this.StateOfResidence = StateOfResidence;
    }

    public void setWatchListed(String WatchListed) {
        this.WatchListed = WatchListed;
    }

    public void setBase64Image(String Base64Image) {
        this.Base64Image = Base64Image;
    }

    @Override
    public String toString() {
        return "BvnResponseModel{" + "ResponseCode=" + ResponseCode + ", BVN=" + BVN + ", FirstName=" + FirstName + ", MiddleName=" + MiddleName + ", LastName=" + LastName + ", DateOfBirth=" + DateOfBirth + ", RegistrationDate=" + RegistrationDate + ", EnrollmentBank=" + EnrollmentBank + ", EnrollmentBranch=" + EnrollmentBranch + ", Email=" + Email + ", Gender=" + Gender + ", LevelOfAccount=" + LevelOfAccount + ", LgaOfOrigin=" + LgaOfOrigin + ", LgaOfResidence=" + LgaOfResidence + ", MaritalStatus=" + MaritalStatus + ", NIN=" + NIN + ", NameOnCard=" + NameOnCard + ", Nationality=" + Nationality + ", PhoneNumber1=" + PhoneNumber1 + ", PhoneNumber2=" + PhoneNumber2 + ", ResidentialAddress=" + ResidentialAddress + ", StateOfOrigin=" + StateOfOrigin + ", StateOfResidence=" + StateOfResidence + ", WatchListed=" + WatchListed + ", Base64Image=" + Base64Image + '}';
    }

}
