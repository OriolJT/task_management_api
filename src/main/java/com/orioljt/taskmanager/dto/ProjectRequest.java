package com.orioljt.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String name
) {}
