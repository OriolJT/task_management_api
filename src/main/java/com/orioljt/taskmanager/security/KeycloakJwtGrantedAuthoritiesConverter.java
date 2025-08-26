package com.orioljt.taskmanager.security;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extracts Spring Security {@link GrantedAuthority} values from a Keycloak JWT.
 *
 * <p>Aggregates roles from {@code realm_access.roles} and {@code resource_access[client].roles}
 * (for either the configured client or all clients when none is configured) and maps them to {@code
 * ROLE_*} authorities. Also maps space-delimited {@code scope} into {@code SCOPE_*} authorities.
 */
public class KeycloakJwtGrantedAuthoritiesConverter
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  private final String resourceClientId;

  public KeycloakJwtGrantedAuthoritiesConverter() {
    this.resourceClientId = null;
  }

  public KeycloakJwtGrantedAuthoritiesConverter(String resourceClientId) {
    this.resourceClientId = resourceClientId;
  }

  /**
   * Converts a Keycloak {@link Jwt} into a collection of authorities derived from realm roles,
   * client roles, and scopes.
   *
   * @param jwt decoded JWT token from the resource server
   * @return an ordered, de-duplicated set of authorities (ROLE_* and SCOPE_*)
   */
  @Override
  public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
    Set<String> roles = new HashSet<>();

    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess != null) {
      Object rs = realmAccess.get("roles");
      if (rs instanceof Collection<?> coll) {
        for (Object r : coll) {
          if (r != null) roles.add(String.valueOf(r));
        }
      }
    }

    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
    if (resourceAccess != null) {
      if (resourceClientId != null && resourceAccess.containsKey(resourceClientId)) {
        roles.addAll(extractClientRoles(resourceAccess.get(resourceClientId)));
      } else {
        for (Object client : resourceAccess.values()) {
          roles.addAll(extractClientRoles(client));
        }
      }
    }

    Set<GrantedAuthority> authorities =
        roles.stream()
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> s.startsWith("ROLE_") ? s : ("ROLE_" + s.toUpperCase(Locale.ROOT)))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    String scope = jwt.getClaim("scope");
    if (scope != null) {
      for (String sc : scope.split(" ")) {
        if (!sc.isBlank()) authorities.add(new SimpleGrantedAuthority("SCOPE_" + sc));
      }
    }

    return authorities;
  }

  private Collection<String> extractClientRoles(Object clientAccess) {
    List<String> result = new ArrayList<>();
    if (clientAccess instanceof Map<?, ?> map) {
      Object rolesObj = map.get("roles");
      if (rolesObj instanceof Collection<?> coll) {
        for (Object r : coll) {
          if (r != null) result.add(String.valueOf(r));
        }
      }
    }
    return result;
  }
}
