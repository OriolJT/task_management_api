package com.orioljt.taskmanager.repository;

import com.orioljt.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByOwnerId(UUID ownerId);
    Page<Project> findAllByOwnerId(UUID ownerId, Pageable pageable);
    Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);
}
