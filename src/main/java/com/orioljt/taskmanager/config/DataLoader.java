package com.orioljt.taskmanager.config;

import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.entity.UserRole;
import com.orioljt.taskmanager.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataLoader {

  private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

  @Bean
  @Profile({"dev", "local"})
  CommandLineRunner seedDefaultUser(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.seed.user.enabled:true}") boolean enabled,
      @Value("${app.seed.user.email:admin@example.com}") String email,
      @Value("${app.seed.user.password:ChangeMeNow!}") String rawPassword) {
    return args -> {
      if (!enabled) {
        log.info("Default user seeding disabled (app.seed.user.enabled=false).");
        return;
      }
      createIfMissing(userRepository, passwordEncoder, email, rawPassword);
    };
  }

  @Transactional
  void createIfMissing(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      String email,
      String rawPassword) {
    Optional<User> existing = userRepository.findByEmail(email);
    if (existing.isPresent()) {
      log.info("Default user '{}' already exists. Skipping seed.", email);
      return;
    }

    User user = new User();
    user.setId(UUID.randomUUID());
    user.markNew();
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setRole(UserRole.ADMIN);
    userRepository.save(user);

    log.warn("Created default user '{}'. Please change this password.", email);
  }
}
