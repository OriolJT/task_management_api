package com.orioljt.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @Email @NotBlank @Size(max = 254) String email,
    @NotBlank
        @Size(min = 8, max = 100)
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,100}$",
            message = "password must contain letters and digits and be at least 8 characters")
        String password) {}
