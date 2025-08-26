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
/**
 * Resolves the current authenticated user's local UUID from the Spring Security context.
 *
 * <p>Resolution order:
 *
 * <ol>
 *   <li>If JWT auth: use {@code sub} as UUID when present and user exists locally.
 *   <li>Otherwise, try {@code email} (or {@code preferred_username}) to look up the local user.
 *   <li>If not JWT: try parsing {@link Authentication#getName()} as a UUID.
 *   <li>Fallback to {@code app.dev-user-id} when configured (useful for local/dev).
 * </ol>
 *
 * <p>Throws {@link IllegalStateException} when no local user mapping can be determined and no
 * fallback is configured.
 */
public class CurrentUserProvider {

  private final UserRepository userRepository;

  public CurrentUserProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Value("${app.dev-user-id:}")
  private String devUserId;

  /**
   * Returns the local user id for the current authentication based on JWT subject/email or
   * authentication name, with an optional development fallback.
   *
   * @return the local {@link UUID} of the authenticated user
   * @throws IllegalStateException if the identity cannot be mapped to a local user and no {@code
   *     app.dev-user-id} is configured
   */
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
