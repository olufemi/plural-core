/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.campaign.model;

import java.io.Serializable;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class BaseResponseFireBase implements Serializable {

    private int statusCode;
    private String message;
    private Object data;

    public static BaseResponseFireBase ok(String message, Object data) {
        BaseResponseFireBase r = new BaseResponseFireBase();
        r.setStatusCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static BaseResponseFireBase fail(int code, String message) {
        BaseResponseFireBase r = new BaseResponseFireBase();
        r.setStatusCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }
}

