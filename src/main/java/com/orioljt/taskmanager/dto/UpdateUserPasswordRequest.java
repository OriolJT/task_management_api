package com.orioljt.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Password change payload")
public record UpdateUserPasswordRequest(
    @Schema(description = "New password", example = "ChangeMeNow1")
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword) {}
