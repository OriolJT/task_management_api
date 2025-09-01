package com.orioljt.taskmanager.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class CurrentUserProviderTest {

  UserRepository userRepository;
  CurrentUserProvider provider;

  @BeforeEach
  void setup() {
    userRepository = Mockito.mock(UserRepository.class);
    provider = new CurrentUserProvider(userRepository);
    SecurityContextHolder.clearContext();
  }

  @Test
  void jwtWithUuidSub_returnsThatId_whenExists() {
    UUID uid = UUID.randomUUID();
  Jwt jwt = Jwt.withTokenValue("t").subject(uid.toString()).header("alg", "none").claim("email", "a@b.com").build();
  java.util.Collection<org.springframework.security.core.GrantedAuthority> auths = java.util.List.of(() -> "ROLE_USER");
  JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, auths);
    when(userRepository.existsById(uid)).thenReturn(true);
    SecurityContextHolder.getContext().setAuthentication(auth);
    assertThat(provider.getCurrentUserId()).isEqualTo(uid);
  }

  @Test
  void jwtWithEmail_fallsBackToEmailLookup() {
    UUID uid = UUID.randomUUID();
  Jwt jwt = Jwt.withTokenValue("t").subject("not-a-uuid").header("alg", "none").claim("email", "x@y.com").build();
  java.util.Collection<org.springframework.security.core.GrantedAuthority> auths = java.util.List.of(() -> "ROLE_USER");
  JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, auths);
    User u = new User();
    u.setId(uid);
    u.setEmail("x@y.com");
    when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.of(u));
    SecurityContextHolder.getContext().setAuthentication(auth);
    assertThat(provider.getCurrentUserId()).isEqualTo(uid);
  }

  @Test
  void nameAuthentication_parsesUuid() {
    UUID uid = UUID.randomUUID();
    var auth = new TestingAuthenticationToken(uid.toString(), "n/a");
    auth.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(auth);
    assertThat(provider.getCurrentUserId()).isEqualTo(uid);
  }

  @Test
  void noAuth_andNoDevId_throws() {
    assertThatThrownBy(() -> provider.getCurrentUserId()).isInstanceOf(IllegalStateException.class);
  }
}
