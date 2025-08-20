package com.orioljt.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.ProjectService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        @Bean ProjectService projectService() { return Mockito.mock(ProjectService.class); }
        @Bean JwtUserProvisioningFilter jwtUserProvisioningFilter() { return Mockito.mock(JwtUserProvisioningFilter.class); }
        @Bean KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter() { return Mockito.mock(KeycloakJwtGrantedAuthoritiesConverter.class); }
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        UUID id = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        ProjectResponse resp = new ProjectResponse(id, "ProjectOne", owner, Instant.now());
        when(service.create(any())).thenReturn(resp);

        mvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ProjectRequest("ProjectOne"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/projects/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("ProjectOne"));
    }

    @Test
    void list_shouldReturnOk() throws Exception {
        ProjectResponse resp = new ProjectResponse(UUID.randomUUID(), "ProjectOne", UUID.randomUUID(), Instant.now());
        when(service.list()).thenReturn(List.of(resp));
        mvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
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
        mvc.perform(delete("/api/projects/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateName_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        ProjectResponse resp = new ProjectResponse(id, "NewName", UUID.randomUUID(), Instant.now());
        when(service.updateName(any(), any())).thenReturn(resp);
        mvc.perform(patch("/api/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ProjectRequest("NewName"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }
}