package com.orioljt.taskmanager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies that unknown sort properties are sanitized and default to createdAt, DESC in links.
 */
@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerSortSanitizeTest {

  @Autowired MockMvc mvc;
  @Autowired ProjectService service;

  @TestConfiguration
  static class Cfg {
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
  void list_withUnknownSort_defaultsToCreatedAtDesc_inLinkHeader() throws Exception {
    when(service.page(any(Pageable.class)))
        .thenAnswer(
            inv -> {
              Pageable p = inv.getArgument(0);
              ProjectResponse resp =
                  new ProjectResponse(
                      UUID.randomUUID(), "P1", UUID.randomUUID(), Instant.now());
              Page<ProjectResponse> page = new PageImpl<>(List.of(resp), p, 1);
              return page;
            });

    mvc.perform(get("/api/projects").param("sort", "unknown,asc"))
        .andExpect(status().isOk())
        .andExpect(header().string("Link", org.hamcrest.Matchers.containsString("sort=createdAt,DESC")));
  }
}
