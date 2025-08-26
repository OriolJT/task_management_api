package com.orioljt.taskmanager.mapper;

import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.Task;
import com.orioljt.taskmanager.entity.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

  public Task toNewEntity(TaskRequest request, Project project) {
    Task task = new Task();
    task.setTitle(request.title());
    task.setDescription(request.description());
    task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);
    task.setPriority(request.priority());
    task.setDueDate(request.dueDate());
    project.addTask(task);
    return task;
  }

  public void updateEntity(Task task, TaskRequest request) {
    if (request.title() != null) task.setTitle(request.title());
    if (request.description() != null) task.setDescription(request.description());
    if (request.status() != null) task.setStatus(request.status());
    if (request.priority() != null) task.setPriority(request.priority());
    if (request.dueDate() != null) task.setDueDate(request.dueDate());
  }

  public TaskResponse toResponse(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getTitle(),
        task.getDescription(),
        task.getStatus(),
        task.getPriority(),
        task.getDueDate(),
        task.getProject().getId(),
        task.getCreatedAt());
  }
}
