package com.orioljt.taskmanager.dto;

import com.orioljt.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(min = 3, max = 200) String title,
        @Size(max = 2000) String description,
        TaskStatus status,
        @Min(1) @Max(3) Integer priority,
        LocalDate dueDate
) {}
