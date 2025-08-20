package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.Task;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.TaskRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import com.orioljt.taskmanager.mapper.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepository tasks;
    private final ProjectRepository projects;
    private final CurrentUserProvider currentUser;
    private final TaskMapper mapper;

    public TaskService(TaskRepository tasks,
                       ProjectRepository projects,
                       CurrentUserProvider currentUser,
                       TaskMapper mapper) {
        this.tasks = tasks;
        this.projects = projects;
        this.currentUser = currentUser;
        this.mapper = mapper;
    }

    public TaskResponse create(UUID projectId, TaskRequest taskRequest) {
        Project project = requireOwnedProject(projectId);

    Task task = mapper.toNewEntity(taskRequest, project);
    return mapper.toResponse(tasks.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(UUID projectId) {
        Project project = requireOwnedProject(projectId);
    return tasks.findAllByProjectIdOrderByCreatedAtDesc(project.getId())
        .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(UUID projectId, UUID taskId) {
    requireOwnedProject(projectId);
        Task t = tasks.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    return mapper.toResponse(t);
    }

    public TaskResponse update(UUID projectId, UUID taskId, TaskRequest taskRequest) {
        requireOwnedProject(projectId);
        Task task = tasks.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

    mapper.updateEntity(task, taskRequest);
    return mapper.toResponse(tasks.save(task));
    }

    public void delete(UUID projectId, UUID taskId) {
        requireOwnedProject(projectId);
        Task t = tasks.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        tasks.delete(t);
    }

    private Project requireOwnedProject(UUID projectId) {
        UUID ownerId = currentUser.getCurrentUserId();
        return projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found or not owned by current user"));
    }

    // Mapping moved to TaskMapper
}
