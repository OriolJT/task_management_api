package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.controller.util.PaginationUtil;
import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Validated
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "Tasks", description = "Operations on tasks within a project")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping
  @Operation(summary = "Create a task", description = "Creates a new task under the given project")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Created",
        content = @Content(schema = @Schema(implementation = TaskResponse.class))),
    @ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public TaskResponse create(
      @PathVariable UUID projectId, @RequestBody @Valid TaskRequest request) {
    return taskService.create(projectId, request);
  }

  @GetMapping
  @Operation(
      summary = "List tasks",
      description = "Returns a page of tasks with pagination headers; sorting is sanitized.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        headers = {
          @Header(
              name = "Link",
              description = "Pagination links (first, last, prev, next)",
              schema = @Schema(type = "string")),
          @Header(
              name = "X-Total-Count",
              description = "Total items count",
              schema = @Schema(type = "integer", format = "int64"))
        },
        content =
            @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))),
    @ApiResponse(responseCode = "404", description = "Project not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<List<TaskResponse>> list(
      @PathVariable UUID projectId,
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size (1-100)", example = "20")
          @RequestParam(defaultValue = "20")
          int size,
      @Parameter(
              description = "Sort directives (multi-valued): field,dir",
              array = @ArraySchema(arraySchema = @Schema(description = "e.g. createdAt,desc")))
          @RequestParam(defaultValue = "createdAt,desc")
          List<String> sort,
      UriComponentsBuilder uriBuilder) {

    Sort requested =
        Sort.by(
            sort.stream()
                .map(
                    s -> {
                      String[] parts = s.split(",");
                      String property = parts[0];
                      Sort.Direction dir =
                          parts.length > 1
                              ? Sort.Direction.fromOptionalString(parts[1])
                                  .orElse(Sort.Direction.ASC)
                              : Sort.Direction.ASC;
                      return new Sort.Order(dir, property);
                    })
                .toList());

    Sort safeSort =
        PaginationUtil.sanitizeSort(
            requested, Set.of("createdAt", "title", "status", "priority", "dueDate", "id"));
    Pageable pageable =
        PageRequest.of(
            Math.max(0, page),
            Math.min(size, 100),
            safeSort.isUnsorted() ? Sort.by(Sort.Direction.DESC, "createdAt") : safeSort);

    Page<TaskResponse> result = taskService.page(projectId, pageable);
    var headers =
        PaginationUtil.generatePaginationHttpHeaders(
            uriBuilder.path("/api/projects/" + projectId + "/tasks"), result);
    return ResponseEntity.ok().headers(headers).body(result.getContent());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a task")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public TaskResponse get(@PathVariable UUID projectId, @PathVariable UUID id) {
    return taskService.get(projectId, id);
  }

  @PatchMapping("/{id}")
  @Operation(
      summary = "Update a task",
      description = "Partial update; only non-null fields are applied")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public TaskResponse update(
      @PathVariable UUID projectId,
      @PathVariable UUID id,
      @RequestBody @Valid TaskRequest request) {
    return taskService.update(projectId, id, request);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a task")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID id) {
    taskService.delete(projectId, id);
    return ResponseEntity.noContent().build();
  }
}
