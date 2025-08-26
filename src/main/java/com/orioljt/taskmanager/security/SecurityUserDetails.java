package com.orioljt.taskmanager.security;

import com.orioljt.taskmanager.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class SecurityUserDetails {

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return username ->
        userRepository
            .findByEmail(username)
            .map(
                u ->
                    User.withUsername(u.getEmail())
                        .password(u.getPassword())
                        .roles(u.getRole() != null ? u.getRole().name() : "USER")
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
