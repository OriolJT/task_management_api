package com.orioljt.taskmanager.security;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.entity.UserRole;
import com.orioljt.taskmanager.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that auto-provisions a local {@link com.orioljt.taskmanager.entity.User} from a validated
 * JWT.
 *
 * <p>Runs once per request after JWT authentication has populated the {@link
 * org.springframework.security.core.context.SecurityContextHolder}. If the user referenced by the
 * JWT does not exist, a new {@code User} is created with a random placeholder password and a role
 * derived from authorities.
 *
 * <p>Uses {@code sub} (UUID) as primary key when possible, falling back to the {@code email} or
 * {@code preferred_username} claims.
 */
@Component
public class JwtUserProvisioningFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtUserProvisioningFilter.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public JwtUserProvisioningFilter(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuth
        && authentication.isAuthenticated()) {
      Jwt jwt = jwtAuth.getToken();
      String sub = jwt.getSubject();
      String email = jwt.getClaim("email");
      if (email == null || email.isBlank()) {
        email = jwt.getClaim("preferred_username");
      }

      try {
        UUID userId = UUID.fromString(sub);
        Optional<User> existing = userRepository.findById(userId);
        if (existing.isEmpty()) {
          User user = new User();
          user.setId(userId);
          if (email == null || email.isBlank()) {
            email = userId + "@local";
          }
          user.setEmail(email);
          // Random encoded placeholder; passwords aren't used with JWT
          user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
          // role mapping: default USER; ADMIN if authority present
          UserRole role =
              jwtAuth.getAuthorities().stream()
                      .map(GrantedAuthority::getAuthority)
                      .anyMatch(a -> a.equals("ROLE_ADMIN"))
                  ? UserRole.ADMIN
                  : UserRole.USER;
          user.setRole(role);
          user.markNew();
          userRepository.save(user);
          log.info("Provisioned local user {} from JWT.", userId);
        }
      } catch (IllegalArgumentException ex) {
        // sub is not a UUID
        if (email != null && !email.isBlank()) {
          Optional<User> existingByEmail = userRepository.findByEmail(email);
          if (existingByEmail.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            UserRole role =
                jwtAuth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> a.equals("ROLE_ADMIN"))
                    ? UserRole.ADMIN
                    : UserRole.USER;
            user.setRole(role);
            user.markNew();
            userRepository.save(user);
            log.info("Provisioned local user '{}' (non-UUID sub) from JWT.", email);
          }
        } else {
          log.debug(
              "JWT subject is not a UUID and no email claim present; skipping auto-provisioning.");
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
