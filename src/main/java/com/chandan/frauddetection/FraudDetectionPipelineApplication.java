package com.chandan.frauddetection;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FraudDetectionPipelineApplication {

  public static void main(String[] args) {
    SpringApplication.run(FraudDetectionPipelineApplication.class, args);
  }
}
