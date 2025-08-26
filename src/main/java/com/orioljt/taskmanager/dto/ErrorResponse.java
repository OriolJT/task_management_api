package com.orioljt.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Schema(description = "Standard error response envelope")
public record ErrorResponse(
    @Schema(description = "Timestamp in ISO-8601", example = "2025-08-20T10:15:30.123Z")
        String timestamp,
    @Schema(description = "HTTP status code", example = "404") int status,
    @Schema(description = "Reason phrase", example = "Not Found") String error,
    @Schema(description = "Human friendly message", example = "Project not found") String message,
    @Schema(description = "Field level validation errors: field -> messages")
        Map<String, List<String>> fieldErrors) {
  public static ErrorResponse of(
      int status, String error, String message, Map<String, List<String>> fieldErrors) {
    return new ErrorResponse(Instant.now().toString(), status, error, message, fieldErrors);
  }
}
