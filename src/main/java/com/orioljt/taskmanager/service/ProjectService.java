package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import com.orioljt.taskmanager.mapper.ProjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ProjectMapper mapper;

    public ProjectService(ProjectRepository projects,
                          UserRepository users,
                          CurrentUserProvider currentUser,
                          ProjectMapper mapper) {
        this.projects = projects;
        this.users = users;
        this.currentUser = currentUser;
        this.mapper = mapper;
    }

    public ProjectResponse create(ProjectRequest request) {
        UUID ownerId = currentUser.getCurrentUserId();
        User owner = users.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner user not found: " + ownerId));

    Project project = mapper.toNewEntity(request, owner);
    return mapper.toResponse(projects.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list() {
        UUID ownerId = currentUser.getCurrentUserId();
    return projects.findAllByOwnerId(ownerId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> page(Pageable pageable) {
        UUID ownerId = currentUser.getCurrentUserId();
        return projects.findAllByOwnerId(ownerId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID projectId) {
        UUID ownerId = currentUser.getCurrentUserId();
        Project p = projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    return mapper.toResponse(p);
    }

    public ProjectResponse updateName(UUID projectId, ProjectRequest request) {
        UUID ownerId = currentUser.getCurrentUserId();
        Project project = projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    mapper.update(project, request);
    return mapper.toResponse(projects.save(project));
    }

    public void delete(UUID projectId) {
        UUID ownerId = currentUser.getCurrentUserId();
        Project p = projects.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    projects.delete(p);
    }
}