package com.chandan.frauddetection.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DlqRecordTest {

  @Test
  void kafkaPositionCreatesDeterministicIdentityAndReplayIsOneWay() {
    DlqRecord first = new DlqRecord("key", "{}", "failure", "events-dlq", 1, 42L);
    DlqRecord redelivery = new DlqRecord("key", "{}", "failure", "events-dlq", 1, 42L);

    assertThat(first.getId()).isEqualTo(redelivery.getId());
    assertThat(first.getStatus()).isEqualTo(DlqStatus.OPEN);

    first.replayed();
    assertThat(first.getStatus()).isEqualTo(DlqStatus.REPLAYED);
    assertThatThrownBy(first::replayed).isInstanceOf(IllegalStateException.class);
  }
}
