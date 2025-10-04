package com.finacial.wealth.api.fxpeer.exchange.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class BaseResponse implements Serializable {

    private static final long serialVersionUID = -5175606879269762371L;

    private int statusCode;

    private String description;

    private Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public BaseResponse() {
    }

    public BaseResponse(int statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addData(String name, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(name, value);

    }
}
