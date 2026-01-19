/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.fxpeer.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class LiquidationApprovalRequest {
    @NotBlank
    private String orderRef;
}

