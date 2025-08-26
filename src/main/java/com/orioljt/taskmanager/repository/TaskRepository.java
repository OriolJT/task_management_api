package com.orioljt.taskmanager.repository;

import com.orioljt.taskmanager.entity.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
  List<Task> findAllByProjectIdOrderByCreatedAtDesc(UUID projectId);

  Page<Task> findAllByProjectId(UUID projectId, Pageable pageable);

  Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);
}
