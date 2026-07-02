package com.chandan.frauddetection.service;

import com.chandan.frauddetection.dto.TransactionRequest;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class RequestFingerprintService {

  public String fingerprint(TransactionRequest r) {
    String c =
        r.accountId().trim()
            + "|"
            + r.amount().stripTrailingZeros().toPlainString()
            + "|"
            + r.currency().toUpperCase();
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(c.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
