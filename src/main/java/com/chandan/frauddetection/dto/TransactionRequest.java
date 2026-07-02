package com.chandan.frauddetection.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransactionRequest(
    @NotBlank @Size(max = 120) String idempotencyKey,
    @NotBlank @Size(max = 80) String accountId,
    @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 4) BigDecimal amount,
    @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {}
