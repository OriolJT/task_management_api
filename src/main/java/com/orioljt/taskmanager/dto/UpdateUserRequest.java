package com.orioljt.taskmanager.dto;

import com.orioljt.taskmanager.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Update fields for a user; all fields optional")
public record UpdateUserRequest(
        @Schema(description = "New email address")
        @Email(message = "email must be a valid email address") String email,

        @Schema(description = "Optional role (admin use only)")
        UserRole role,

        @Schema(description = "Optional new password (use dedicated endpoint for self-change)")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters") String password
) {}
