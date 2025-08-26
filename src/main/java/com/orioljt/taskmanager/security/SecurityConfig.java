package com.orioljt.taskmanager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  private static final String[] SWAGGER_WHITELIST = {
    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationConverter jwtAuthConverter,
      JwtUserProvisioningFilter provisioningFilter)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(SWAGGER_WHITELIST)
                    .permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users")
                    .anonymous()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
        .addFilterAfter(provisioningFilter, BearerTokenAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter(
      KeycloakJwtGrantedAuthoritiesConverter authoritiesConverter) {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return converter;
  }

  @Bean
  public KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter(
      @Value("${app.security.oauth2.client-id:}") String clientId) {
    if (clientId == null || clientId.isBlank()) {
      return new KeycloakJwtGrantedAuthoritiesConverter();
    }
    return new KeycloakJwtGrantedAuthoritiesConverter(clientId);
  }
}
