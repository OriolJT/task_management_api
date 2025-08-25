package com.orioljt.taskmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orioljt.taskmanager.dto.CreateUserRequest;
import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.TestJwtDecoderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestJwtDecoderConfig.class)
class SecurityIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository users;

    @Test
    void securedEndpointsRequireAuth() throws Exception {
        mvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationIsOpen() throws Exception {
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new CreateUserRequest("int-user@example.com", "Password123"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void userTokenCanCreateAndListOwnProjects() throws Exception {
        UUID uid = UUID.randomUUID();
        String token = "user_" + uid;

        mvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new ProjectRequest("My First Project"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(uid.toString()))
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/projects/.*")));

        mvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value(uid.toString()));

        assertThat(users.findById(uid)).isPresent();
    }

    @Test
    void adminTokenCanAccessAdminEndpointsAndUserTokenCannot() throws Exception {
        User u = new User();
        u.setEmail("fetchme@example.com");
        u.setPassword("Password123");
        users.save(u);

        String adminToken = "admin_" + UUID.randomUUID();
        String userToken = "user_" + UUID.randomUUID();

        mvc.perform(get("/api/admin/users/{id}", u.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(u.getId().toString()));

        mvc.perform(get("/api/admin/users/{id}", u.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotAccessAccountEndpoints() throws Exception {
        mvc.perform(get("/api/account"))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/account").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/account/password").contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"Password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotPatchAdminUpdateUser() throws Exception {
        User u = new User();
        u.setEmail("to-update@example.com");
        u.setPassword("Password123");
        users.save(u);

        String userToken = "user_" + UUID.randomUUID();
        mvc.perform(patch("/api/admin/users/{id}", u.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.com\"}"))
                .andExpect(status().isForbidden());
    }
}
