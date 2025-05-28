package com.one.core.application.exception;

import org.springframework.http.HttpStatus;
import java.util.Map; // Opcional, para devolver múltiples errores de campo

public class ValidationException extends ApiException {
    // Opcional: para errores más detallados por campo
    private Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}