package com.orioljt.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.*;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserService userService;
    @Autowired JwtUserProvisioningFilter jwtUserProvisioningFilter;
    @Autowired KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter;

    @TestConfiguration
    static class Mocks {
        @Bean UserService userService() { return Mockito.mock(UserService.class); }
        @Bean JwtUserProvisioningFilter jwtUserProvisioningFilter() { return Mockito.mock(JwtUserProvisioningFilter.class); }
        @Bean KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter() { return Mockito.mock(KeycloakJwtGrantedAuthoritiesConverter.class); }
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse resp = new UserResponse(id, "u@e", Instant.now());
        when(userService.register(any())).thenReturn(resp);
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new CreateUserRequest("u@e", "Password123"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/" + id));
    }

    @Test
    void getAccount_shouldReturnUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse resp = new UserResponse(id, "u@e", Instant.now());
        when(userService.getCurrentUser()).thenReturn(resp);
        mvc.perform(get("/api/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void updatePassword_shouldReturnNoContent() throws Exception {
        mvc.perform(patch("/api/account/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UpdateUserPasswordRequest("NewPassword1"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void admin_getUser_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse resp = new UserResponse(id, "u@e", Instant.now());
        when(userService.getUser(id)).thenReturn(resp);
        mvc.perform(get("/api/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}
