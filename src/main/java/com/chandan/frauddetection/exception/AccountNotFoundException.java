package com.chandan.frauddetection.exception;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(String accountId) {
    super("Account not found: " + accountId);
  }
}
