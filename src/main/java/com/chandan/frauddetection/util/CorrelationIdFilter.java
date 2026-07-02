package com.chandan.frauddetection.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ensures every request carries a correlation ID - either passed in by the caller/gateway, or
 * generated here. Placed in MDC so every log line for the request (and every downstream Kafka
 * event) can be tied together during an incident investigation.
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

  public static final String HEADER_NAME = "X-Correlation-Id";
  public static final String MDC_KEY = "correlationId";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String correlationId = httpRequest.getHeader(HEADER_NAME);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    MDC.put(MDC_KEY, correlationId);
    httpResponse.setHeader(HEADER_NAME, correlationId);

    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
