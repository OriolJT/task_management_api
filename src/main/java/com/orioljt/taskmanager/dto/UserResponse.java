package com.orioljt.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "User resource representation")
public record UserResponse(
    @Schema(description = "User id") UUID id,
    @Schema(description = "Email address") String email,
    @Schema(description = "Creation timestamp") Instant createdAt) {}
