package com.chandan.frauddetection.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account implements Serializable {

  @Id
  @Column(name = "account_id", length = 80)
  private String accountId;

  @Column(name = "owner_name", nullable = false, length = 120)
  private String ownerName;

  @Column(name = "risk_tier", nullable = false, length = 20)
  private String riskTier;

  @Column(name = "daily_limit", nullable = false, precision = 19, scale = 4)
  private BigDecimal dailyLimit;

  protected Account() {}

  public Account(String id, String owner, String risk, BigDecimal limit) {
    accountId = id;
    ownerName = owner;
    riskTier = risk;
    dailyLimit = limit;
  }

  public String getAccountId() {
    return accountId;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public String getRiskTier() {
    return riskTier;
  }

  public BigDecimal getDailyLimit() {
    return dailyLimit;
  }
}
