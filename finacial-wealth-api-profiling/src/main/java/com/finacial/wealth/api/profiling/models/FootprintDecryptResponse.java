/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models;

import java.util.Map;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class FootprintDecryptResponse {

    private String dob;
    private String lastName;
    private String ssn4;
    private String firstName;
    private String country;
    private String middleName;
    

    // Getters and setters
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSsn4() { return ssn4; }
    public void setSsn4(String ssn4) { this.ssn4 = ssn4; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String mobile;
    public String email;
}
