package com.orioljt.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.exception.GlobalExceptionHandler;
import com.orioljt.taskmanager.security.JwtUserProvisioningFilter;
import com.orioljt.taskmanager.security.KeycloakJwtGrantedAuthoritiesConverter;
import com.orioljt.taskmanager.service.ProjectService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProjectControllerValidationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired ProjectService projectService;

    @TestConfiguration
    static class Mocks {
        @Bean ProjectService projectService() { return Mockito.mock(ProjectService.class); }
        @Bean JwtUserProvisioningFilter jwtUserProvisioningFilter() { return Mockito.mock(JwtUserProvisioningFilter.class); }
        @Bean KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter() { return Mockito.mock(KeycloakJwtGrantedAuthoritiesConverter.class); }
    }

    @Test
    void create_withBlankName_returns400WithFieldError() throws Exception {
        mvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ProjectRequest("  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").isArray());
    }

    @Test
    void list_withInvalidPaging_returns400WithFieldErrors() throws Exception {
        mvc.perform(get("/api/projects")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors['list.page']").isArray())
                .andExpect(jsonPath("$.fieldErrors['list.size']").isArray());
    }
}
