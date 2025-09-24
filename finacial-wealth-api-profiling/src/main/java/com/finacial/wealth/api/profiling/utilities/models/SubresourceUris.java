/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.utilities.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class SubresourceUris {

    @SerializedName("media")
    @Expose
    private String media;

}
