package com.orioljt.taskmanager.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        UUID ownerId,
        Instant createdAt
) {}
