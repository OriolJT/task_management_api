package com.orioljt.taskmanager.security;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.repository.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

  private final UserRepository userRepository;

  public CurrentUserProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Value("${app.dev-user-id:}")
  private String devUserId;

  public UUID getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth && auth.isAuthenticated()) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getSubject();
      try {
        UUID uid = UUID.fromString(sub);
        if (userRepository.existsById(uid)) return uid;
      } catch (Exception ignored) {
      }
      String email = jwt.getClaim("email");
      if (email == null || email.isBlank()) email = jwt.getClaim("preferred_username");
      if (email != null && !email.isBlank()) {
        final String lookupEmail = email;
        return userRepository
            .findByEmail(lookupEmail)
            .map(User::getId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Authenticated user not found locally for email: " + lookupEmail));
      }
    } else if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
      try {
        return UUID.fromString(auth.getName());
      } catch (Exception ignored) {
      }
    }
    if (devUserId != null && !devUserId.isBlank()) {
      return UUID.fromString(devUserId);
    }
    throw new IllegalStateException(
        "No authenticated user mapped to local user and no app.dev-user-id configured");
  }
}
