package com.chandan.frauddetection.exception;

public class IdempotencyConflictException extends RuntimeException {

  public IdempotencyConflictException(String k) {
    super("Idempotency key was reused with a different request: " + k);
  }
}
