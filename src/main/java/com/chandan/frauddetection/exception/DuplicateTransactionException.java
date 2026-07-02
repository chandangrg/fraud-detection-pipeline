package com.chandan.frauddetection.exception;

/**
 * Thrown when an idempotency key has already been processed. Callers should treat this as "safe to
 * ignore" - the original result stands - not as an error requiring a retry with a new key.
 */
public class DuplicateTransactionException extends RuntimeException {

  public DuplicateTransactionException(String idempotencyKey) {
    super("Transaction with idempotencyKey [" + idempotencyKey + "] was already processed");
  }
}
