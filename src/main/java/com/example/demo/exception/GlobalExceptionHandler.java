package com.example.demo.exception;

import com.example.demo.dto.response.GlobalApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Bắt lỗi 404
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ResponseEntity<GlobalApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        GlobalApiResponse<Object> response = GlobalApiResponse.error(
            HttpStatus.NOT_FOUND.value(), 
            ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Bắt lỗi 409 (Conflict)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<GlobalApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.warn("Data integrity violation: {}", ex.getMessage());
        GlobalApiResponse<Object> response = GlobalApiResponse.error(
            HttpStatus.CONFLICT.value(), 
            ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Bắt lỗi 400 (Bad Request)
    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<GlobalApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        GlobalApiResponse<Object> response = GlobalApiResponse.error(
            HttpStatus.BAD_REQUEST.value(), 
            ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // Bắt lỗi 403 (Forbidden)
    @ExceptionHandler(ForbiddenException.class)
    @ResponseBody
    public ResponseEntity<GlobalApiResponse<Object>> handleForbidden(ForbiddenException ex) {
        logger.warn("Forbidden: {}", ex.getMessage());
        GlobalApiResponse<Object> response = GlobalApiResponse.error(
            HttpStatus.FORBIDDEN.value(), 
            ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // Bắt lỗi validation (@Valid) - Rất quan trọng
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<GlobalApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        logger.warn("Validation error: {}", errorMessage);
        GlobalApiResponse<Object> response = GlobalApiResponse.error(
            HttpStatus.BAD_REQUEST.value(), 
            errorMessage
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Bắt tất cả các lỗi 500 (Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<GlobalApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Internal server error: ", ex);
        GlobalApiResponse<Object> response = GlobalApiResponse.error(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), 
            "An unexpected internal server error occurred. Please try again later."
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}