package com.chandan.frauddetection.repository;

import com.chandan.frauddetection.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {}
