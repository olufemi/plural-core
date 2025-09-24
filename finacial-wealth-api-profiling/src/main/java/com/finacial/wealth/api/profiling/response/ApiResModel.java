/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.response;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * @author olufemioshin
 */
@Data
public class ApiResModel {

	private int statusCode;

	private String response_message;

	private String response_code;

	private Map<String, Object> data = new HashMap<>();

	private String description;

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getResponse_message() {
		return response_message;
	}

	public void setResponse_message(String response_message) {
		this.response_message = response_message;
	}

	public String getResponse_code() {
		return response_code;
	}

	public void setResponse_code(String response_code) {
		this.response_code = response_code;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public ApiResModel() {
	}

	public ApiResModel(int statusCode, String description) {
		this(statusCode, description, null);
	}

	public ApiResModel(int statusCode, String response_message, Map<String, Object> data) {
		this.statusCode = statusCode;
		this.response_message = response_message;
		this.data = data;
	}

	public void addData(String name, Object value) {
		if (data == null) {
			data = new HashMap<String, Object>();
		}
		data.put(name, value);

	}

}
