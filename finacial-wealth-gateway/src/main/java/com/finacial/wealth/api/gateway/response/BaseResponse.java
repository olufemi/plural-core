package com.finacial.wealth.api.gateway.response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BaseResponse implements Serializable {

    private Map<String, Object> data = new HashMap<String, Object>();
    private int statusCode;
    private String description;

    public BaseResponse() {
    }

    public BaseResponse(int statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addData(String name, Object value) {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        data.put(name, value);

    }

}
