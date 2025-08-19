package com.orioljt.taskmanager.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String resourceClientId;

    public KeycloakJwtGrantedAuthoritiesConverter() {
        this.resourceClientId = null; // include all client roles
    }

    public KeycloakJwtGrantedAuthoritiesConverter(String resourceClientId) {
        this.resourceClientId = resourceClientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roles = new HashSet<>();

        // realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Object rs = realmAccess.get("roles");
            if (rs instanceof Collection<?> coll) {
                for (Object r : coll) {
                    if (r != null) roles.add(String.valueOf(r));
                }
            }
        }

        // client roles
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

        // map to ROLE_ authorities (uppercased)
        Set<GrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("ROLE_") ? s : ("ROLE_" + s.toUpperCase(Locale.ROOT)))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // scopes if present
        String scope = jwt.getClaim("scope");
        if (scope != null) {
            for (String sc : scope.split(" ")) {
                if (!sc.isBlank()) authorities.add(new SimpleGrantedAuthority("SCOPE_" + sc));
            }
        }

        return authorities;
    }

    @SuppressWarnings("unchecked")
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

