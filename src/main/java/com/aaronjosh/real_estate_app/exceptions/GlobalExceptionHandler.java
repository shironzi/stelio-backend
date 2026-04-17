package com.aaronjosh.real_estate_app.exceptions;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.PessimisticLockException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private Map<String, Object> createErrorResponse(HttpStatus status, String message) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("timestamp", LocalDateTime.now());
                response.put("status", status.value());
                response.put("error", status.getReasonPhrase());
                response.put("message", message);
                return response;
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<?> handleEmailConflict(EmailAlreadyExistsException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "error", "Conflict",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(PasswordNotMatchException.class)
        public ResponseEntity<?> handlePasswordMismatch(PasswordNotMatchException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of(
                                                "error", "Bad Request",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

                return ResponseEntity.badRequest()
                                .body(Map.of("error", "Bad Request", "messages", errors));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleOtherExceptions(Exception ex) {

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "An unexpected error occurred"));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", ex.getStatusCode().value());
                body.put("error", ex.getStatusCode());
                body.put("message", ex.getReason());

                return new ResponseEntity<>(body, ex.getStatusCode());
        }

        @ExceptionHandler({
                        DataIntegrityViolationException.class,
                        PessimisticLockException.class,
                        CannotAcquireLockException.class
        })
        public ResponseEntity<?> handleConflictExceptions(Exception ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "error", "Conflict",
                                                "message", "Conflict detected. Please try again."));
        }
}
