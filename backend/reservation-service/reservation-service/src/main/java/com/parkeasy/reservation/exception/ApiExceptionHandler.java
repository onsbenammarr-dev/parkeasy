package com.parkeasy.reservation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SlotUnavailableException.class)
    public ResponseEntity<Map<String, Object>> slotUnavailable(SlotUnavailableException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", e.getMessage());
        body.put("alternativeSpots", e.getAlternativeSpots());
        body.put("nextSlot", e.getNextSlot());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}