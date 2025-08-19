package com.orioljt.taskmanager.security;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.entity.UserRole;
import com.orioljt.taskmanager.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUserProvisioningFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtUserProvisioningFilter.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public JwtUserProvisioningFilter(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated()) {
            Jwt jwt = jwtAuth.getToken();
            String sub = jwt.getSubject();
            String email = jwt.getClaim("email");
            if (email == null || email.isBlank()) {
                email = jwt.getClaim("preferred_username");
            }

            try {
                UUID userId = UUID.fromString(sub);
                Optional<User> existing = users.findById(userId);
                if (existing.isEmpty()) {
                    User user = new User();
                    user.setId(userId);
                    if (email == null || email.isBlank()) {
                        email = userId.toString() + "@local";
                    }
                    user.setEmail(email);
                    // Random encoded placeholder; passwords aren't used with JWT
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    // role mapping: default USER; ADMIN if authority present
                    UserRole role = jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                            .anyMatch(a -> a.equals("ROLE_ADMIN")) ? UserRole.ADMIN : UserRole.USER;
                    user.setRole(role);
                    users.save(user);
                    log.info("Provisioned local user {} from JWT.", userId);
                }
            } catch (IllegalArgumentException ex) {
                // sub is not a UUID
                if (email != null && !email.isBlank()) {
                    Optional<User> existingByEmail = users.findByEmail(email);
                    if (existingByEmail.isEmpty()) {
                        User user = new User();
                        user.setEmail(email);
                        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                        UserRole role = jwtAuth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                                .anyMatch(a -> a.equals("ROLE_ADMIN")) ? UserRole.ADMIN : UserRole.USER;
                        user.setRole(role);
                        users.save(user);
                        log.info("Provisioned local user '{}' (non-UUID sub) from JWT.", email);
                    }
                } else {
                    log.debug("JWT subject is not a UUID and no email claim present; skipping auto-provisioning.");
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
