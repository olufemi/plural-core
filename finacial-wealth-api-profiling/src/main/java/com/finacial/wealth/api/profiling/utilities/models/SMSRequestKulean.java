/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class SMSRequestKulean {

    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("destination")
    @Expose
    private List<String> destination = null;
    @SerializedName("text")
    @Expose
    private String text;

}
