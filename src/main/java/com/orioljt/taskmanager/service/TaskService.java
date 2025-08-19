package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.Task;
import com.orioljt.taskmanager.entity.TaskStatus;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.TaskRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
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

    public TaskService(TaskRepository tasks,
                       ProjectRepository projects,
                       CurrentUserProvider currentUser) {
        this.tasks = tasks;
        this.projects = projects;
        this.currentUser = currentUser;
    }

    public TaskResponse create(UUID projectId, TaskRequest taskRequest) {
        Project project = requireOwnedProject(projectId);

        Task task = new Task();
        task.setTitle(taskRequest.title());
        task.setDescription(taskRequest.description());
        task.setStatus(taskRequest.status() != null ? taskRequest.status() : TaskStatus.TODO);
        task.setPriority(taskRequest.priority());
        task.setDueDate(taskRequest.dueDate());
        task.setProject(project);

        return toDto(tasks.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(UUID projectId) {
        Project project = requireOwnedProject(projectId);
        return tasks.findAllByProjectIdOrderByCreatedAtDesc(project.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(UUID projectId, UUID taskId) {
        requireOwnedProject(projectId); // ensures scope & authorization
        Task t = tasks.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        return toDto(t);
    }

    public TaskResponse update(UUID projectId, UUID taskId, TaskRequest taskRequest) {
        requireOwnedProject(projectId);
        Task task = tasks.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (taskRequest.title() != null) {
            task.setTitle(taskRequest.title());
        }
        if (taskRequest.description() != null) {
            task.setDescription(taskRequest.description());
        }
        if (taskRequest.status() != null) {
            task.setStatus(taskRequest.status());
        }
        if (taskRequest.priority() != null) {
            task.setPriority(taskRequest.priority());
        }
        if (taskRequest.dueDate() != null) {
            task.setDueDate(taskRequest.dueDate());
        }

        return toDto(tasks.save(task));
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

    private TaskResponse toDto(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getCreatedAt()
        );
    }
}
