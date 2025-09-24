/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.models;

import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class AddedMembersFE {

    private String memberId;
    // private String memberIdType;
    //private String memberName;
    //private String memberUserId;
    //private String memberType;
    private String invitationCodeReqId;
    private String adminEmailAddress;

}
