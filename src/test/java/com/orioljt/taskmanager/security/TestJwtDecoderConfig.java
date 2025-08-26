package com.orioljt.taskmanager.security;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestJwtDecoderConfig {

  @Bean
  @Primary
  public JwtDecoder jwtDecoder() {
    return token -> {
      String role = "user";
      String sub = UUID.randomUUID().toString();
      if (token != null) {
        String[] parts;
        if (token.contains(":")) {
          parts = token.split(":", 2);
        } else if (token.contains("_")) {
          parts = token.split("_", 2);
        } else {
          parts = null;
        }
        if (parts != null) {
          String maybeRole = parts[0];
          String maybeSub = parts[1];
          if (maybeSub != null && !maybeSub.isBlank()) sub = maybeSub;
          if ("admin".equalsIgnoreCase(maybeRole)) role = "admin";
        }
      }
      Map<String, Object> claims = new HashMap<>();
      claims.put("sub", sub);
      claims.put("email", sub + "@example.com");
      claims.put("realm_access", Map.of("roles", List.of(role)));
      return Jwt.withTokenValue(token == null ? "t" : token)
          .header("alg", "none")
          .claims(map -> map.putAll(claims))
          .issuedAt(Instant.now())
          .expiresAt(Instant.now().plusSeconds(3600))
          .build();
    };
  }
}
