/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.storage.firebase;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class BaseResponseFireBase {

    private int statusCode;
    private String description;
    private Object data;

    public static BaseResponseFireBase ok(String msg, Object data) {
        BaseResponseFireBase r = new BaseResponseFireBase();
        r.setStatusCode(200);
        r.setDescription(msg);
        r.setData(data);
        return r;
    }

    public static BaseResponseFireBase fail(int code, String msg) {
        BaseResponseFireBase r = new BaseResponseFireBase();
        r.setStatusCode(code);
        r.setDescription(msg);
        r.setData(null);
        return r;
    }
}
