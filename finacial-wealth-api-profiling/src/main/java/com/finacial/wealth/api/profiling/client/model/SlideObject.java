/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.client.model;

/**
 *
 * @author olufemioshin
 */
import lombok.Data;
import java.io.Serializable;

@Data
public class SlideObject implements Serializable {
    private String fileName; // can be "slides/xxx.pdf" OR "xxx.pdf"
}

