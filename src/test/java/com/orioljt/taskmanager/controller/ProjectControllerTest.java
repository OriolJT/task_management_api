package com.orioljt.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.ProjectService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;
  @Autowired ProjectService service;
  @Autowired JwtUserProvisioningFilter jwtUserProvisioningFilter;
  @Autowired KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter;

  @TestConfiguration
  static class Mocks {
    @Bean
    ProjectService projectService() {
      return Mockito.mock(ProjectService.class);
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
  void create_shouldReturnCreated() throws Exception {
    UUID id = UUID.randomUUID();
    UUID owner = UUID.randomUUID();
    ProjectResponse resp = new ProjectResponse(id, "ProjectOne", owner, Instant.now());
    when(service.create(any())).thenReturn(resp);

    mvc.perform(
            post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ProjectRequest("ProjectOne"))))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/projects/" + id))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("ProjectOne"));
  }

  @Test
  void list_shouldReturnOk() throws Exception {
    ProjectResponse resp =
        new ProjectResponse(UUID.randomUUID(), "ProjectOne", UUID.randomUUID(), Instant.now());
    org.springframework.data.domain.Page<com.orioljt.taskmanager.dto.ProjectResponse> page =
        new org.springframework.data.domain.PageImpl<>(
            List.of(resp), org.springframework.data.domain.PageRequest.of(0, 20), 1);
    when(service.page(
            org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(page);

    mvc.perform(get("/api/projects"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", "1"))
        .andExpect(
            header()
                .string(
                    "Link", org.hamcrest.Matchers.containsString("/api/projects?page=0&size=20")))
        .andExpect(header().string("Link", org.hamcrest.Matchers.containsString("rel=\"first\"")))
        .andExpect(jsonPath("$[0].name").value("ProjectOne"));
  }

  @Test
  void get_shouldReturnOk() throws Exception {
    UUID id = UUID.randomUUID();
    ProjectResponse resp = new ProjectResponse(id, "P1", UUID.randomUUID(), Instant.now());
    when(service.get(id)).thenReturn(resp);
    mvc.perform(get("/api/projects/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  void delete_shouldReturnNoContent() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(delete("/api/projects/{id}", id)).andExpect(status().isNoContent());
  }

  @Test
  void updateName_shouldReturnOk() throws Exception {
    UUID id = UUID.randomUUID();
    ProjectResponse resp = new ProjectResponse(id, "NewName", UUID.randomUUID(), Instant.now());
    when(service.updateName(any(), any())).thenReturn(resp);
    mvc.perform(
            patch("/api/projects/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new ProjectRequest("NewName"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("NewName"));
  }
}
