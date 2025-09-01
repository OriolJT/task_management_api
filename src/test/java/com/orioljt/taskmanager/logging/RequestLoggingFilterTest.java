package com.orioljt.taskmanager.logging;

import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequestLoggingFilterTest {

  @Test
  void filter_runsAndDoesNotLogSensitiveHeaders() throws ServletException, IOException {
    RequestLoggingFilter filter = new RequestLoggingFilter();
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse res = Mockito.mock(HttpServletResponse.class);
    FilterChain chain = Mockito.mock(FilterChain.class);

    Mockito.when(req.getMethod()).thenReturn("GET");
    Mockito.when(req.getRequestURI()).thenReturn("/ping");
    Mockito.when(res.getStatus()).thenReturn(200);
    Mockito.when(req.getHeaderNames()).thenReturn(new java.util.Vector<>(java.util.List.of("Authorization", "X-Request-Id")).elements());
    Mockito.when(req.getHeader("Authorization")).thenReturn("secret");
    Mockito.when(req.getHeader("X-Request-Id")).thenReturn("rid");

    assertThatCode(() -> filter.doFilter(req, res, chain)).doesNotThrowAnyException();
    Mockito.verify(chain).doFilter(req, res);
  }
}
