package com.orioljt.taskmanager.dto;

import com.orioljt.taskmanager.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        Integer priority,
        LocalDate dueDate,
        UUID projectId,
        Instant createdAt
) {}
