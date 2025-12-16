/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.dto;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class LoginStep2Request {
  private String challengeId;
  private String code;
}
