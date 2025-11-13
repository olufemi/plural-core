/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.config;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalApiErrorHandler {

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponseModel> onMissingHeader(MissingRequestHeaderException ex) {
        ApiResponseModel resp = new ApiResponseModel();

        resp.setStatusCode(400);
        resp.setDescription("Missing required header: " + ex.getHeaderName());
        resp.setDescription("Provide the '" + ex.getHeaderName() + "' header.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseModel> onValidation(MethodArgumentNotValidException ex) {
        ApiResponseModel resp = new ApiResponseModel();

        resp.setStatusCode(400);
        resp.setDescription("Validation error");
        resp.setDescription(ex.getBindingResult().toString());
        return ResponseEntity.badRequest().body(resp);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseModel> onAny(Exception ex) {
        ApiResponseModel resp = new ApiResponseModel();

        resp.setStatusCode(500);
        resp.setDescription("Internal error");
        resp.setDescription(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
    }
}
