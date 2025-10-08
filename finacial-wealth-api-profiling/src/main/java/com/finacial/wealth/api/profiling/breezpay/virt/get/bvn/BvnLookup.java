/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.bvn;

/**
 *
 * @author olufemioshin
 */
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bvn_lookup", uniqueConstraints = {
        @UniqueConstraint(name = "UK_BVN_LOOKUP_BVN", columnNames = "bvn")
})
@Getter @Setter
public class BvnLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 11, nullable = false)
    private String bvn;

    @Column(length = 100)
    private String nameOnCard;

    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(length = 50)
    private String lastName;

    private LocalDate dateOfBirth;         // parsed from "dd-MMM-yyyy"
    private LocalDate registrationDate;    // parsed from "dd-MMM-yyyy"

    @Column(length = 20)
    private String phoneNumber1;

    @Column(length = 20)
    private String phoneNumber2;

    @Column(length = 10)
    private String enrollmentBank;

    @Column(length = 100)
    private String enrollmentBranch;

    @Column(length = 120)
    private String email;

    @Column(length = 10)
    private String gender;

    @Column(length = 60)
    private String levelOfAccount;

    @Column(length = 60)
    private String lgaOfOrigin;

    @Column(length = 60)
    private String lgaOfResidence;

    @Column(length = 20)
    private String maritalStatus;

    @Column(length = 30)
    private String nin;

    @Column(length = 60)
    private String nationality;

    @Column(length = 255)
    private String residentialAddress;

    @Column(length = 60)
    private String stateOfOrigin;

    @Column(length = 60)
    private String stateOfResidence;

    @Column(length = 20)
    private String title;

    @Column(length = 5)
    private String watchListed;            // YES / NO

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String base64Image;

    @Column(length = 5)
    private String responseCode;           // e.g., "00"

    // bookkeeping
    private OffsetDateTime createdAt;
    private OffsetDateTime lastCheckedAt;
}

