/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.security.consent;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author olufemioshin
 */
public final class ConsentJsonBuilder {

    private final List<String> fields = new ArrayList<>();

    private ConsentJsonBuilder() {
    }

    public static ConsentJsonBuilder create() {
        return new ConsentJsonBuilder();
    }

    public ConsentJsonBuilder addString(String key, Object value) {
        fields.add("\"" + key + "\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(value)) + "\"");
        return this;
    }

    public ConsentJsonBuilder addBooleanAsString(String key, boolean value) {
        fields.add("\"" + key + "\":\"" + value + "\"");
        return this;
    }

    public ConsentJsonBuilder addNumberAsString(String key, Object value) {
        fields.add("\"" + key + "\":\"" + ConsentStringUtil.esc(ConsentStringUtil.nz(value)) + "\"");
        return this;
    }

    public ConsentJsonBuilder addBoolean(String key, boolean value) {
        fields.add("\"" + key + "\":" + value);
        return this;
    }

    public ConsentJsonBuilder addNumber(String key, Object value) {
        fields.add("\"" + key + "\":" + ConsentStringUtil.nz(value));
        return this;
    }

    public String build() {
        return "{" + String.join(",", fields) + "}";
    }
}