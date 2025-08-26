package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.controller.util.PaginationUtil;
import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.service.ProjectService;
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
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
    ProjectResponse created = projectService.create(request);
    return ResponseEntity.created(URI.create("/api/projects/" + created.id())).body(created);
  }

  @GetMapping
  public ResponseEntity<List<ProjectResponse>> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
      @RequestParam(defaultValue = "createdAt,desc") List<String> sort,
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
  public ResponseEntity<ProjectResponse> get(@PathVariable UUID id) {
    return ResponseEntity.ok(projectService.get(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    projectService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ProjectResponse> updateName(
      @PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
    return ResponseEntity.ok(projectService.updateName(id, request));
  }
}
