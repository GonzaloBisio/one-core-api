package com.one.core.application.exception;

import org.springframework.http.HttpStatus;

public class DuplicateFieldException extends ApiException {
    public DuplicateFieldException(String fieldName, String fieldValue) {
        super(String.format("%s '%s' already exists.", fieldName, fieldValue), HttpStatus.CONFLICT);
    }

    public DuplicateFieldException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}