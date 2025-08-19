package com.orioljt.taskmanager.controller;

import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponse create(@PathVariable UUID projectId,
                               @RequestBody @Valid TaskRequest request) {
        return taskService.create(projectId, request);
    }

    @GetMapping
    public List<TaskResponse> list(@PathVariable UUID projectId) {
        return taskService.list(projectId);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable UUID projectId, @PathVariable UUID id) {
        return taskService.get(projectId, id);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable UUID projectId,
                               @PathVariable UUID id,
                               @RequestBody @Valid TaskRequest request) {
        return taskService.update(projectId, id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID projectId, @PathVariable UUID id) {
        taskService.delete(projectId, id);
    }
}
