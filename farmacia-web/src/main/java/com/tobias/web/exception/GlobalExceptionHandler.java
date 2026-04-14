package com.tobias.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", e.getClass().getSimpleName());
        error.put("message", e.getMessage());

        // Stack trace para debugging
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            error.put("location", stackTrace[0].toString());
        }

        // Log completo en el servidor
        System.err.println("ERROR: " + e.getClass().getName() + ": " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
