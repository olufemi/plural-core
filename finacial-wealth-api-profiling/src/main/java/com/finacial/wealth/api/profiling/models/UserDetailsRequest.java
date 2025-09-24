/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.models;

import com.google.gson.annotations.Expose;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author olufemioshin
 */
//@Data
public class UserDetailsRequest {

    @ApiModelProperty(notes = "The phoneNumber")
    @NotNull(message = "the field \"phoneNumber\" is not nillable")
    @NotBlank
    @Expose
    private String phoneNumber;
    @ApiModelProperty(notes = "The emailAddress")
    @NotNull(message = "the field \"emailAddress\" is not nillable")
    @NotBlank
    @Expose
    private String emailAddress;
    @ApiModelProperty(notes = "The password")
    @NotNull(message = "the field \"password\" is not nillable")
    @NotBlank
    @Expose
    private String password;
    @ApiModelProperty(notes = "The confPassword")
    @NotNull(message = "the field \"confPassword\" is not nillable")
    @NotBlank
    @Expose
    private String confPassword;
    @ApiModelProperty(notes = "The userName")
    @NotNull(message = "the field \"userName\" is not nillable")
    @NotBlank
    @Expose
    private String userName;
    @ApiModelProperty(notes = "The firstName")
    @NotNull(message = "the field \"firstName\" is not nillable")
    @NotBlank
    @Expose
    private String firstName;
    @ApiModelProperty(notes = "The lastName")
    @NotNull(message = "the field \"lastName\" is not nillable")
    @NotBlank
    @Expose
    private String lastName;

    @ApiModelProperty(notes = "The userGroup")
    @NotNull(message = "the field \"userGroup\" is not nillable")
    @NotBlank
    @Expose
    private String userGroup;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfPassword() {
        return confPassword;
    }

    public void setConfPassword(String confPassword) {
        this.confPassword = confPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }
    
    

}
