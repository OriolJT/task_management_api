package com.orioljt.taskmanager.dto;

import com.orioljt.taskmanager.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Task creation/update payload")
public record TaskRequest(
        @Schema(description = "Short title", example = "Implement OpenAPI polish")
        @NotBlank @Size(min = 3, max = 200) String title,
        @Schema(description = "Detailed description", example = "Add error schema, tags and examples")
        @Size(max = 2000) String description,
        @Schema(description = "Task status; null defaults to TODO", example = "IN_PROGRESS")
        TaskStatus status,
        @Schema(description = "Priority 1 (high) - 3 (low)", example = "1")
        @Min(1) @Max(3) Integer priority,
        @Schema(description = "Due date ISO-8601", example = "2025-09-01")
        @FutureOrPresent(message = "dueDate must be today or a future date") LocalDate dueDate
) {}
