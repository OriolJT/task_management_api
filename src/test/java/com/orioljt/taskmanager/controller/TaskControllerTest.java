package com.orioljt.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.entity.TaskStatus;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.TaskService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired TaskService service;
    @Autowired JwtUserProvisioningFilter jwtUserProvisioningFilter;
    @Autowired KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter;

    @TestConfiguration
    static class Mocks {
        @Bean TaskService taskService() { return Mockito.mock(TaskService.class); }
        @Bean JwtUserProvisioningFilter jwtUserProvisioningFilter() { return Mockito.mock(JwtUserProvisioningFilter.class); }
        @Bean KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter() { return Mockito.mock(KeycloakJwtGrantedAuthoritiesConverter.class); }
    }

    @Test
    void create_shouldReturnTask() throws Exception {
        UUID projectId = UUID.randomUUID();
        TaskResponse resp = new TaskResponse(UUID.randomUUID(), "Task1", "D", TaskStatus.TODO, 1, LocalDate.now(), projectId, Instant.now());
        when(service.create(eq(projectId), any())).thenReturn(resp);
        mvc.perform(post("/api/projects/{pid}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new TaskRequest("Task1", "D", TaskStatus.TODO, 1, LocalDate.now()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task1"));
    }

    @Test
    void list_shouldReturnTasks() throws Exception {
    UUID projectId = UUID.randomUUID();
    TaskResponse resp = new TaskResponse(UUID.randomUUID(), "T1", null, TaskStatus.TODO, 1, null, projectId, Instant.now());
    org.springframework.data.domain.Page<com.orioljt.taskmanager.dto.TaskResponse> page =
        new org.springframework.data.domain.PageImpl<>(List.of(resp), org.springframework.data.domain.PageRequest.of(0, 20), 1);
    when(service.page(eq(projectId), org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
    mvc.perform(get("/api/projects/{pid}/tasks", projectId))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Total-Count", "1"))
        .andExpect(header().string("Link", org.hamcrest.Matchers.containsString("/api/projects/" + projectId + "/tasks?page=0&size=20")))
        .andExpect(jsonPath("$[0].projectId").value(projectId.toString()));
    }

    @Test
    void get_shouldReturnTask() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TaskResponse resp = new TaskResponse(id, "T1", null, TaskStatus.TODO, 1, null, projectId, Instant.now());
        when(service.get(projectId, id)).thenReturn(resp);
        mvc.perform(get("/api/projects/{pid}/tasks/{id}", projectId, id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void update_shouldReturnTask() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TaskResponse resp = new TaskResponse(id, "New", "D", TaskStatus.DONE, 2, LocalDate.now(), projectId, Instant.now());
        when(service.update(eq(projectId), eq(id), any())).thenReturn(resp);
        mvc.perform(patch("/api/projects/{pid}/tasks/{id}", projectId, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new TaskRequest("New", "D", TaskStatus.DONE, 2, LocalDate.now()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/projects/{pid}/tasks/{id}", projectId, id))
                .andExpect(status().isOk());
    }
}
