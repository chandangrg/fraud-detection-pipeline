package com.chandan.frauddetection.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminApiKeyFilter implements Filter {

  private final String key;

  public AdminApiKeyFilter(@Value("${app.admin.api-key:change-me}") String key) {
    this.key = key;
  }

  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest r = (HttpServletRequest) req;
    if (r.getRequestURI().startsWith("/api/v1/admin")
        && !key.equals(r.getHeader("X-Admin-Api-Key"))) {
      ((HttpServletResponse) res).sendError(401);
      return;
    }
    chain.doFilter(req, res);
  }
}
