package com.chandan.frauddetection.controller;

import com.chandan.frauddetection.dto.*;
import com.chandan.frauddetection.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService s;

  public TransactionController(TransactionService s) {
    this.s = s;
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> submit(@Valid @RequestBody TransactionRequest r) {
    var result = s.submit(r);
    return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(TransactionResponse.from(result.transaction()));
  }

  @GetMapping("/{id}")
  public TransactionResponse get(@PathVariable String id) {
    return TransactionResponse.from(s.get(id));
  }
}
