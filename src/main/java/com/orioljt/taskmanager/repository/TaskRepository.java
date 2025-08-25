package com.orioljt.taskmanager.repository;

import com.orioljt.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);
    Page<Task> findAllByProjectId(UUID projectId, Pageable pageable);
    Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);
}
