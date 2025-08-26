package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.Task;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.mapper.TaskMapper;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.TaskRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final CurrentUserProvider currentUserProvider;
  private final TaskMapper taskMapper;

  public TaskService(
      TaskRepository taskRepository,
      ProjectRepository projectRepository,
      CurrentUserProvider currentUserProvider,
      TaskMapper taskMapper) {
    this.taskRepository = taskRepository;
    this.projectRepository = projectRepository;
    this.currentUserProvider = currentUserProvider;
    this.taskMapper = taskMapper;
  }

  public TaskResponse create(UUID projectId, TaskRequest taskRequest) {
    Project project = requireOwnedProject(projectId);

    Task task = taskMapper.toNewEntity(taskRequest, project);
    return taskMapper.toResponse(taskRepository.save(task));
  }

  @Transactional(readOnly = true)
  public List<TaskResponse> list(UUID projectId) {
    Project project = requireOwnedProject(projectId);
    return taskRepository.findAllByProjectIdOrderByCreatedAtDesc(project.getId()).stream()
        .map(taskMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public Page<TaskResponse> page(UUID projectId, Pageable pageable) {
    requireOwnedProject(projectId);
    return taskRepository.findAllByProjectId(projectId, pageable).map(taskMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public TaskResponse get(UUID projectId, UUID taskId) {
    requireOwnedProject(projectId);
    Task task =
        taskRepository
            .findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new NotFoundException("Task not found"));
    return taskMapper.toResponse(task);
  }

  public TaskResponse update(UUID projectId, UUID taskId, TaskRequest taskRequest) {
    requireOwnedProject(projectId);
    Task task =
        taskRepository
            .findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new NotFoundException("Task not found"));

    taskMapper.updateEntity(task, taskRequest);
    return taskMapper.toResponse(taskRepository.save(task));
  }

  public void delete(UUID projectId, UUID taskId) {
    Project project = requireOwnedProject(projectId);
    Task task =
        taskRepository
            .findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new NotFoundException("Task not found"));
    project.removeTask(task);
    projectRepository.save(project);
  }

  private Project requireOwnedProject(UUID projectId) {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    return projectRepository
        .findByIdAndOwnerId(projectId, ownerId)
        .orElseThrow(() -> new NotFoundException("Project not found or not owned by current user"));
  }
}
