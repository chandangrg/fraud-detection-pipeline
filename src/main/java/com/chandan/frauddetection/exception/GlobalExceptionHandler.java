package com.chandan.frauddetection.exception;

import java.time.Instant;
import java.util.*;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({AccountNotFoundException.class, FraudDecisionNotFoundException.class})
  ResponseEntity<?> notFound(RuntimeException e) {
    return body(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler({LimitExceededException.class, MethodArgumentNotValidException.class})
  ResponseEntity<?> bad(Exception e) {
    return body(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(IdempotencyConflictException.class)
  ResponseEntity<?> conflict(RuntimeException e) {
    return body(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<?> missing(RuntimeException e) {
    return body(HttpStatus.NOT_FOUND, e.getMessage());
  }

  private ResponseEntity<Map<String, Object>> body(HttpStatus s, String m) {
    Map<String, Object> b = new LinkedHashMap<>();
    b.put("timestamp", Instant.now());
    b.put("status", s.value());
    b.put("message", m);
    b.put("correlationId", MDC.get("correlationId"));
    return ResponseEntity.status(s).body(b);
  }
}
