package com.chandan.frauddetection.config;

import com.chandan.frauddetection.entity.Account;
import com.chandan.frauddetection.repository.AccountRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer {

  @Bean
  CommandLineRunner seedSyntheticAccounts(AccountRepository accounts) {
    return args -> {
      if (!accounts.existsById("acct-low")) {
        accounts.save(
            new Account("acct-low", "Synthetic Customer", "LOW", new BigDecimal("25000.00")));
      }
      if (!accounts.existsById("acct-high")) {
        accounts.save(
            new Account(
                "acct-high", "Synthetic Review Customer", "HIGH", new BigDecimal("15000.00")));
      }
    };
  }
}
