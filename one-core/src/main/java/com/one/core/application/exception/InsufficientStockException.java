package com.one.core.application.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends RuntimeException {


    public InsufficientStockException(String message) {
        super();
    }
}
