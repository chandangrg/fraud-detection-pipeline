package com.chandan.frauddetection.dto;

import com.chandan.frauddetection.entity.Transaction;

public record TransactionSubmissionResult(Transaction transaction, boolean created) {}
