package com.orioljt.taskmanager.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple centralized request logging filter (safe fields only). Produces a single structured log
 * line per request.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final Set<String> SENSITIVE_HEADERS =
      Set.of("authorization", "cookie", "set-cookie", "proxy-authorization");

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    long start = System.nanoTime();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationMs = (System.nanoTime() - start) / 1_000_000;
      Map<String, Object> safeHeaders = new HashMap<>();
      for (Enumeration<String> e = request.getHeaderNames(); e != null && e.hasMoreElements(); ) {
        String name = e.nextElement();
        if (!SENSITIVE_HEADERS.contains(name.toLowerCase())) {
          safeHeaders.put(name, request.getHeader(name));
        }
      }
      log.info(
          "request_log timestamp={} method={} path={} status={} durationMs={} headers={}",
          Instant.now().toString(),
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          durationMs,
          safeHeaders);
    }
  }
}
