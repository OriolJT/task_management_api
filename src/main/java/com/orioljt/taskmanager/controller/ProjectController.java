package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse created = projectService.create(request);
        return ResponseEntity
                .created(URI.create("/api/projects/" + created.id()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list() {
        return ResponseEntity.ok(projectService.list());
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
    public ResponseEntity<ProjectResponse> updateName(@PathVariable UUID id,
                                                      @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateName(id, request));
    }
}
