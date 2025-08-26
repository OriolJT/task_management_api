package com.orioljt.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Project creation/update payload")
public record ProjectRequest(
    @Schema(description = "Project name", example = "Website revamp")
        @NotBlank
        @Size(min = 3, max = 100)
        String name) {}
