package com.chandan.frauddetection.entity;

public enum OutboxStatus {
  PENDING,
  IN_FLIGHT,
  RETRY,
  PUBLISHED,
  DEAD_LETTER,
}
