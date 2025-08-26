package com.orioljt.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Project resource representation")
public record ProjectResponse(
    @Schema(description = "Project id") UUID id,
    @Schema(description = "Project name") String name,
    @Schema(description = "Owner user id") UUID ownerId,
    @Schema(description = "Creation timestamp") Instant createdAt) {}
