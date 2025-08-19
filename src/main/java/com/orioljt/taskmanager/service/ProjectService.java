package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;
    private final CurrentUserProvider currentUser;

    public ProjectService(ProjectRepository projects,
                          UserRepository users,
                          CurrentUserProvider currentUser) {
        this.projects = projects;
        this.users = users;
        this.currentUser = currentUser;
    }

    public ProjectResponse create(ProjectRequest request) {
        UUID ownerId = currentUser.getCurrentUserId();
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner user not found: " + ownerId));

        Project project = new Project();
        project.setName(request.name());
        project.setOwner(owner);

        return toDto(projects.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        UUID ownerId = currentUser.getCurrentUserId();
        return projects.findAllByOwnerId(ownerId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID projectId) {
        UUID ownerId = currentUser.getCurrentUserId();
        Project p = projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        return toDto(p);
    }

    public ProjectResponse updateName(UUID projectId, ProjectRequest request) {
        UUID ownerId = currentUser.getCurrentUserId();
        Project project = projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        project.setName(request.name());
        return toDto(projects.save(project));
    }

    public void delete(UUID projectId) {
        UUID ownerId = currentUser.getCurrentUserId();
        Project p = projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        projects.delete(p);
    }

    private ProjectResponse toDto(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getOwner().getId(),
                project.getCreatedAt()
        );
    }
}