/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.models;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ProfilingLimitsResponse {

    private String accountNumber;
    private String currency;
    private String tier;
    private Limits limits;

    @Data
    public static class Limits {

        private Direction send;
        private Direction receive;
    }

    @Data
    public static class Direction {

        private Limit single;
        private Limit daily;
        private Limit weekly;
        private Limit monthly;
    }

    @Data
    public static class Limit {

        private String label;
        private String total; // nullable
        private String spent; // nullable
        private String left; // nullable
        private boolean unlimited;
    }
}
