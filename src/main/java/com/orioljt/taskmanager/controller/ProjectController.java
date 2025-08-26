package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.controller.util.PaginationUtil;
import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.service.ProjectService;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@Validated
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Operations on projects owned by the authenticated user")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping
  @Operation(
      summary = "Create a project",
      description = "Creates a new project for the current user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Created",
        headers = {
          @Header(
              name = "Location",
              description = "URI of the created resource",
              schema = @Schema(type = "string"))
        },
        content = @Content(schema = @Schema(implementation = ProjectResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
    ProjectResponse created = projectService.create(request);
    return ResponseEntity.created(URI.create("/api/projects/" + created.id())).body(created);
  }

  @GetMapping
  @Operation(
      summary = "List projects",
      description = "Returns a page of projects. Provides RFC-5988 Link and X-Total-Count headers.")
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
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = ProjectResponse.class)))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<List<ProjectResponse>> list(
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(defaultValue = "0")
          @Min(0)
          int page,
      @Parameter(description = "Page size (1-100)", example = "20")
          @RequestParam(defaultValue = "20")
          @Min(1)
          @Max(100)
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

    Sort safeSort = PaginationUtil.sanitizeSort(requested, Set.of("createdAt", "name", "id"));
    Pageable pageable =
        PageRequest.of(
            page,
            size,
            safeSort.isUnsorted() ? Sort.by(Sort.Direction.DESC, "createdAt") : safeSort);

    Page<ProjectResponse> result = projectService.page(pageable);
    var headers =
        PaginationUtil.generatePaginationHttpHeaders(uriBuilder.path("/api/projects"), result);
    return ResponseEntity.ok().headers(headers).body(result.getContent());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a project")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<ProjectResponse> get(@PathVariable UUID id) {
    return ResponseEntity.ok(projectService.get(id));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a project")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    projectService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}")
  @Operation(
      summary = "Update project name",
      description = "Partial update: only name is supported")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
  })
  public ResponseEntity<ProjectResponse> updateName(
      @PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
    return ResponseEntity.ok(projectService.updateName(id, request));
  }
}
