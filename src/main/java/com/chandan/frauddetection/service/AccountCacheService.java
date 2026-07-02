package com.chandan.frauddetection.service;

import com.chandan.frauddetection.entity.Account;
import com.chandan.frauddetection.exception.AccountNotFoundException;
import com.chandan.frauddetection.repository.AccountRepository;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AccountCacheService {

  private static final Logger log = LoggerFactory.getLogger(AccountCacheService.class);
  private final RedisTemplate<String, Object> redis;
  private final AccountRepository database;
  private final Duration ttl;

  public AccountCacheService(
      RedisTemplate<String, Object> redis,
      AccountRepository database,
      @Value("${app.cache.account-ttl-seconds:120}") long ttlSeconds) {
    this.redis = redis;
    this.database = database;
    this.ttl = Duration.ofSeconds(ttlSeconds);
  }

  public Account getAccount(String accountId) {
    String cacheKey = "account:" + accountId;
    String loadLockKey = cacheKey + ":load";
    try {
      Object hit = redis.opsForValue().get(cacheKey);
      if (hit instanceof Account account) return account;

      Boolean lockOwner = redis.opsForValue().setIfAbsent(loadLockKey, "1", Duration.ofSeconds(3));
      if (Boolean.TRUE.equals(lockOwner)) {
        try {
          Account account = load(accountId);
          redis.opsForValue().set(cacheKey, account, ttl);
          return account;
        } finally {
          redis.delete(loadLockKey);
        }
      }

      Thread.sleep(50);
      Object secondRead = redis.opsForValue().get(cacheKey);
      if (secondRead instanceof Account account) return account;
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    } catch (Exception cacheFailure) {
      log.warn("Redis cache degraded; using database fallback: {}", cacheFailure.getMessage());
    }
    return load(accountId);
  }

  private Account load(String accountId) {
    return database.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
  }
}
