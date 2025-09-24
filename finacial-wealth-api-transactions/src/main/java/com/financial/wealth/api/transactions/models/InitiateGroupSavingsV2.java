/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models;

/**
 *
 * @author olufemioshin
 */
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import lombok.Data;

@Data
public class InitiateGroupSavingsV2 {
    @NotNull(message = "GroupSavingName is required!")
    private String groupSavingName;

    @NotNull(message = "GroupSavingDescription is required!")
    private String groupSavingDescription;

    @NotNull(message = "GroupSavingAmount is required!")
    private String groupSavingAmount;

    @NotNull(message = "AllowPublicToJoin is required!")
    private boolean allowPublicToJoin;

    // NEW: frequency-driven scheduling
    @NotNull(message = "ContributionFrequency is required!")
    private String contributionFrequency; // WEEKLY | BIWEEKLY | MONTHLY | QUARTERLY

    /*@NotBlank(message = "startDate is required! Format: yyyy-MM-dd")
    private String startDate;*/

    // For WEEKLY/BIWEEKLY: 1(Mon) .. 7(Sun)
    private Integer contributionDayOfWeek;

    // For MONTHLY/QUARTERLY: 1..28 (recommend ≤28)
    private Integer contributionDayOfMonth;

    // Optional override for first payout date
    private String firstPayoutDate; // yyyy-MM-dd (optional)

    // Legacy (deprecated). If provided and frequency=MONTHLY, we still accept it.
    private String payOutDateOfTheMonth;

    @NotNull(message = "NumberOfMembers is required!")
    private int numberOfMembers;

    @NotNull(message = "EmailAddress is required!")
    private String emailAddress;

    @ApiModelProperty(notes = "The Selected Slot (admin)")
    @NotNull(message = "the field \"selectedSlot\" is not nillable")
    private int selectedSlot;

    // NEW: choose payout policy
    @NotNull(message = "payoutPolicy is required!")
    private String payoutPolicy; // EACH_CYCLE | PERIOD_END | AFTER_ALL_CONTRIBUTIONS
}
