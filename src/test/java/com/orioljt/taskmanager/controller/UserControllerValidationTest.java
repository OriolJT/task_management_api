package com.orioljt.taskmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.CreateUserRequest;
import com.orioljt.taskmanager.dto.UpdateUserPasswordRequest;
import com.orioljt.taskmanager.dto.UpdateUserRequest;
import com.orioljt.taskmanager.exception.GlobalExceptionHandler;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerValidationTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @TestConfiguration
  static class Mocks {
    @Bean
    UserService userService() {
      return Mockito.mock(UserService.class);
    }

    @Bean
    JwtUserProvisioningFilter jwtUserProvisioningFilter() {
      return Mockito.mock(JwtUserProvisioningFilter.class);
    }

    @Bean
    KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter() {
      return Mockito.mock(KeycloakJwtGrantedAuthoritiesConverter.class);
    }
  }

  @Test
  void register_withWeakPassword_returns400() throws Exception {
    CreateUserRequest bad = new CreateUserRequest("user@example.com", "allletters");
    mvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.password").isArray());
  }

  @Test
  void updateAccount_withInvalidEmail_returns400() throws Exception {
    UpdateUserRequest bad = new UpdateUserRequest("not-an-email", null, null);
    mvc.perform(
            patch("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.email").isArray());
  }

  @Test
  void updateAccountPassword_tooShort_returns400() throws Exception {
    UpdateUserPasswordRequest bad = new UpdateUserPasswordRequest("short");
    mvc.perform(
            patch("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.newPassword").isArray());
  }
}
