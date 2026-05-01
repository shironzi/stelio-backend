package com.aaronjosh.real_estate_app.exceptions;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<?> handleEmailConflict(EmailAlreadyExistsException ex) {
                log.warn("Registration conflict: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "error", "Conflict",
                                                "message", ex.getMessage()));
        }

        @ExceptionHandler(PasswordNotMatchException.class)
        public ResponseEntity<?> handlePasswordMismatch(PasswordNotMatchException ex) {
                log.warn("Password mismatch: {}", ex.getMessage());
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

                log.warn("Validation failed: {}", errors);

                return ResponseEntity.badRequest()
                                .body(Map.of("error", "Bad Request", "messages", errors));
        }

        @ExceptionHandler({
                        DataIntegrityViolationException.class,
                        PessimisticLockException.class,
                        CannotAcquireLockException.class
        })
        public ResponseEntity<?> handleConflictExceptions(Exception ex) {
                log.warn("Database conflict or locking issue: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                                "error", "Conflict",
                                                "message", "Conflict detected. Please try again."));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                String message = String.format("Parameter '%s' should be of type '%s'",
                                ex.getName(), ex.getRequiredType().getSimpleName());

                return ResponseEntity.badRequest().body(Map.of(
                                "error", "Bad Request",
                                "message", message));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", ex.getStatusCode().value());
                body.put("error", ex.getStatusCode());
                body.put("message", ex.getReason());
                return new ResponseEntity<>(body, ex.getStatusCode());
        }
}
