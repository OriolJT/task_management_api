package com.orioljt.taskmanager.repository;

import com.orioljt.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);
    Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);
}
