package com.chandan.frauddetection.exception;

public class FraudDecisionNotFoundException extends RuntimeException {

  public FraudDecisionNotFoundException(String transactionId) {
    super("Fraud decision not found for transaction: " + transactionId);
  }
}
