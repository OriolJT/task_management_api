package com.orioljt.taskmanager.repository;

import com.orioljt.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByOwnerId(UUID ownerId);
    Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);
}
