package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.dto.CreateUserRequest;
import com.orioljt.taskmanager.dto.UpdateUserPasswordRequest;
import com.orioljt.taskmanager.dto.UpdateUserRequest;
import com.orioljt.taskmanager.dto.UserResponse;
import com.orioljt.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api")
@Tag(name = "Users", description = "Registration, self-service account, and admin user ops")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/users")
  @Operation(summary = "Register a user", description = "Public registration endpoint")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Created",
        content = @Content(schema = @Schema(implementation = UserResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
  })
  @PreAuthorize("permitAll()")
  public ResponseEntity<UserResponse> register(
      @Valid @RequestBody CreateUserRequest createUserRequest) {
    UserResponse userResponse = userService.register(createUserRequest);
    return ResponseEntity.created(URI.create("/api/users/" + userResponse.id())).body(userResponse);
  }

  @GetMapping("/account")
  @Operation(summary = "Get my account")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @PreAuthorize("isAuthenticated()")
  public UserResponse getAccount() {
    return userService.getCurrentUser();
  }

  @PatchMapping("/account/password")
  @Operation(summary = "Change my password")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> updateAccountPassword(
      @Valid @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
    userService.updateMyPassword(updateUserPasswordRequest);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/admin/users/{id}")
  @Operation(summary = "Get a user (admin)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse getUser(@PathVariable UUID id) {
    return userService.getUser(id);
  }

  @PatchMapping("/account")
  @Operation(summary = "Update my account", description = "Partial update; non-null fields applied")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @PreAuthorize("isAuthenticated()")
  public UserResponse updateMyAccount(@Valid @RequestBody UpdateUserRequest request) {
    return userService.updateMyAccount(request);
  }

  @PatchMapping("/admin/users/{id}")
  @Operation(
      summary = "Update a user (admin)",
      description = "Partial update; non-null fields applied")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse adminUpdateUser(
      @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
    return userService.adminUpdateUser(id, request);
  }

  @DeleteMapping("/account/projects/{projectId}")
  @Operation(summary = "Remove my project")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> removeMyProject(
      @PathVariable UUID projectId, com.orioljt.taskmanager.service.ProjectService projectService) {
    projectService.delete(projectId);
    return ResponseEntity.noContent().build();
  }
}
