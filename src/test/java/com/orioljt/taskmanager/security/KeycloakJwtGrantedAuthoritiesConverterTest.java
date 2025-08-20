package com.orioljt.taskmanager.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtGrantedAuthoritiesConverterTest {

    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("t")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject(UUID.randomUUID().toString())
                .build();
    }

    @Test
    void convert_shouldMapRealmAndClientRolesToAuthorities() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("realm_access", Map.of("roles", List.of("admin", "user")));
        claims.put("resource_access", Map.of(
                "task-api", Map.of("roles", List.of("manager")),
                "other-client", Map.of("roles", List.of("viewer"))
        ));
        Jwt jwt = buildJwt(claims);
        KeycloakJwtGrantedAuthoritiesConverter converter = new KeycloakJwtGrantedAuthoritiesConverter();

        Collection<GrantedAuthority> auths = converter.convert(jwt);
        Set<String> names = auths.stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
        assertThat(names).contains("ROLE_ADMIN", "ROLE_USER", "ROLE_MANAGER", "ROLE_VIEWER");
    }

    @Test
    void convert_shouldHonorClientFilterAndScopes() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("resource_access", Map.of(
                "task-api", Map.of("roles", List.of("writer")),
                "ignore", Map.of("roles", List.of("noop"))
        ));
        claims.put("scope", "read write");
        Jwt jwt = buildJwt(claims);
        KeycloakJwtGrantedAuthoritiesConverter converter = new KeycloakJwtGrantedAuthoritiesConverter("task-api");
        Collection<GrantedAuthority> auths = converter.convert(jwt);
        Set<String> names = auths.stream().map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
        assertThat(names).contains("ROLE_WRITER", "SCOPE_read", "SCOPE_write");
        assertThat(names).doesNotContain("ROLE_NOOP");
    }
}