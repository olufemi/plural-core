package com.finacial.wealth.api.sessionmanager.response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class BaseResponse implements Serializable {

    private static final long serialVersionUID = -5175606879269762371L;

    private int statusCode;

    private String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    private Map<String, Object> data = new HashMap<String, Object>();

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
