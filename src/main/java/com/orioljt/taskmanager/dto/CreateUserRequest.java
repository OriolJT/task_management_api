package com.orioljt.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration payload")
public record CreateUserRequest(
    @Schema(description = "Email address", example = "user@example.com")
        @Email
        @NotBlank
        @Size(max = 254)
        String email,
    @Schema(description = "Password (letters and digits, min 8)")
        @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,100}$",
            message = "password must contain letters and digits and be at least 8 characters")
        String password) {}
