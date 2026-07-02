package com.chandan.frauddetection.exception;

public class LimitExceededException extends RuntimeException {

  public LimitExceededException(String accountId) {
    super("Transaction amount exceeds daily limit for account: " + accountId);
  }
}
