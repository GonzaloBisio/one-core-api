package com.one.core.application.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable; // Para MethodArgumentNotValidException


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path; // Se podría añadir desde HttpServletRequest
        private Map<String, List<String>> fieldErrors; // Para MethodArgumentNotValidException
    }

    @Data
    @AllArgsConstructor
    private static class SimpleErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
    }


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<SimpleErrorResponse> handleApiException(ApiException ex) {
        SimpleErrorResponse errorDetails = new SimpleErrorResponse(
                LocalDateTime.now(),
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorDetails, ex.getStatus());
    }

    // Específico para DuplicateFieldException si quieres un manejo particular,
    // si no, el manejador de ApiException lo tomará.
    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<SimpleErrorResponse> handleDuplicateFieldException(DuplicateFieldException ex) {
        SimpleErrorResponse errorDetails = new SimpleErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        SimpleErrorResponse errorDetails = new SimpleErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Manejador para ValidationException personalizada (si la usas con fieldErrors)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleCustomValidationException(ValidationException ex) {
        if (ex.getFieldErrors() != null && !ex.getFieldErrors().isEmpty()) {
            // Aquí podrías crear un ErrorResponse más complejo con el mapa de errores
            // Por ahora, un mensaje simple:
            SimpleErrorResponse errorDetails = new SimpleErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    ex.getMessage() // Podrías concatenar los fieldErrors aquí
            );
            return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
        }
        SimpleErrorResponse errorDetails = new SimpleErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    // Manejador para las validaciones de @Valid en los DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        fieldError -> fieldError.getField(),
                        Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
                ));

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation failed. Please check the field errors.",
                null, // Puedes inyectar HttpServletRequest para obtener el path
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Un manejador genérico para otras excepciones no controladas (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleGlobalException(Exception ex) {
        // Loguear el stack trace completo aquí es importante para depuración interna
        // logger.error("Unhandled exception occurred: ", ex);
        SimpleErrorResponse errorDetails = new SimpleErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred. Please try again later." // Mensaje genérico para el cliente
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}