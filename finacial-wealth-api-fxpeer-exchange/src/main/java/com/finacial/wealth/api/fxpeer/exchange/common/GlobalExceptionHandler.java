/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.common;

/**
 *
 * @author olufemioshin
 */
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.Instant;


@RestControllerAdvice
public class GlobalExceptionHandler {


@ExceptionHandler(NotFoundException.class)
public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, jakarta.servlet.http.HttpServletRequest req) {
return ResponseEntity.status(HttpStatus.NOT_FOUND)
.body(new ApiError(ex.getMessage(), req.getRequestURI(), Instant.now()));
}


@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiError> handleBusiness(BusinessException ex, jakarta.servlet.http.HttpServletRequest req) {
return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
.body(new ApiError(ex.getMessage(), req.getRequestURI(), Instant.now()));
}


@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleGeneric(Exception ex, jakarta.servlet.http.HttpServletRequest req) {
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
.body(new ApiError(ex.getMessage(), req.getRequestURI(), Instant.now()));
}
}
