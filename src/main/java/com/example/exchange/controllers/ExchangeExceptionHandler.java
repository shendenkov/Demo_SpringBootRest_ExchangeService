package com.example.exchange.controllers;

import com.example.exchange.exceptions.CommissionException;
import com.example.exchange.exceptions.ExchangeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@ControllerAdvice
public class ExchangeExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({CommissionException.class, ExchangeException.class})
    public ResponseEntity<Object> handleException(CommissionException ex) {
        return wrapError(ex);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return wrapError(ex);
    }

    private ResponseEntity<Object> wrapError(Exception ex) {
        log.error("Catch error: " + ex.getMessage(), ex);
        com.example.exchange.models.Error apiError = new com.example.exchange.models.Error();
        apiError.setDescription(ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}
