package com.chandan.frauddetection.controller;

import com.chandan.frauddetection.entity.*;
import com.chandan.frauddetection.repository.*;
import com.chandan.frauddetection.service.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  private final DlqRecordRepository dlq;
  private final DlqReplayService replay;
  private final OutboxEventRepository outbox;
  private final OutboxStateService outboxState;

  public AdminController(
      DlqRecordRepository dlq,
      DlqReplayService replay,
      OutboxEventRepository outbox,
      OutboxStateService outboxState) {
    this.dlq = dlq;
    this.replay = replay;
    this.outbox = outbox;
    this.outboxState = outboxState;
  }

  @GetMapping("/dlq")
  public List<DlqRecord> dlq() {
    return dlq.findTop100ByStatusOrderByReceivedAtAsc(DlqStatus.OPEN);
  }

  @PostMapping("/dlq/{id}/replay")
  public void replay(@PathVariable String id) {
    replay.replay(id);
  }

  @GetMapping("/outbox")
  public List<OutboxEvent> outbox(@RequestParam(defaultValue = "RETRY") OutboxStatus status) {
    return outbox.findTop100ByStatusOrderByCreatedAtAsc(status);
  }

  @PostMapping("/outbox/{id}/replay")
  public void replayOutbox(@PathVariable String id) {
    outboxState.replay(id);
  }
}
