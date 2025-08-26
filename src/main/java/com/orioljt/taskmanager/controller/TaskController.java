package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.controller.util.PaginationUtil;
import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.service.TaskService;
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
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping
  public TaskResponse create(
      @PathVariable UUID projectId, @RequestBody @Valid TaskRequest request) {
    return taskService.create(projectId, request);
  }

  @GetMapping
  public ResponseEntity<List<TaskResponse>> list(
      @PathVariable UUID projectId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
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
  public TaskResponse get(@PathVariable UUID projectId, @PathVariable UUID id) {
    return taskService.get(projectId, id);
  }

  @PatchMapping("/{id}")
  public TaskResponse update(
      @PathVariable UUID projectId,
      @PathVariable UUID id,
      @RequestBody @Valid TaskRequest request) {
    return taskService.update(projectId, id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID projectId, @PathVariable UUID id) {
    taskService.delete(projectId, id);
    return ResponseEntity.noContent().build();
  }
}
