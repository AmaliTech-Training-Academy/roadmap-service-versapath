package com.capstone.exception;

import com.capstone.dto.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "User not found"));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateUserException(DuplicateUserException ex) {
        log.error("Duplicate user error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate user"));
    }

    @ExceptionHandler(UserProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserProcessingException(UserProcessingException ex) {
        log.error("User processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "User processing failed"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation";
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        // Check for specific constraint violations
        if (rootCause != null) {
            if (rootCause.contains("user_id")) {
                message = "User with this ID already exists";
            } else if (rootCause.contains("email")) {
                message = "User with this email already exists";
            } else if (rootCause.contains("username")) {
                message = "User with this username already exists";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(message, "Data constraint violation"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(errors, "Validation failed"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(errors, "Validation constraint violation"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("An unexpected error occurred", "Internal server error"));
    }
}
