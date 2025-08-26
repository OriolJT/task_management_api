package com.orioljt.taskmanager.dto;

import com.orioljt.taskmanager.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Task resource representation")
public record TaskResponse(
    @Schema(description = "Task id", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
    @Schema(description = "Title", example = "Implement OpenAPI polish") String title,
    @Schema(description = "Description") String description,
    @Schema(description = "Status", example = "TODO") TaskStatus status,
    @Schema(description = "Priority 1 (high) - 3 (low)", example = "2") Integer priority,
    @Schema(description = "Due date", example = "2025-09-01") LocalDate dueDate,
    @Schema(description = "Project id", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID projectId,
    @Schema(description = "Creation timestamp", example = "2025-08-20T10:15:30Z")
        Instant createdAt) {}
