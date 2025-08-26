package com.orioljt.taskmanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.entity.TaskStatus;
import com.orioljt.taskmanager.exception.GlobalExceptionHandler;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.TaskService;
import java.time.LocalDate;
import java.util.UUID;
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

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TaskControllerValidationTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @TestConfiguration
  static class Mocks {
    @Bean
    TaskService taskService() {
      return Mockito.mock(TaskService.class);
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
  void create_withTooShortTitle_returns400() throws Exception {
    UUID projectId = UUID.randomUUID();
    TaskRequest bad = new TaskRequest("aa", null, TaskStatus.TODO, 1, LocalDate.now());
    mvc.perform(
            post("/api/projects/{pid}/tasks", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.title").isArray());
  }

  @Test
  void create_withPastDueDate_returns400() throws Exception {
    UUID projectId = UUID.randomUUID();
    TaskRequest bad =
        new TaskRequest("Valid title", null, TaskStatus.TODO, 1, LocalDate.now().minusDays(1));
    mvc.perform(
            post("/api/projects/{pid}/tasks", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(bad)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.dueDate").isArray());
  }
}
