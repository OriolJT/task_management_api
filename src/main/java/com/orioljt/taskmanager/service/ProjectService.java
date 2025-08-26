package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.mapper.ProjectMapper;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;
  private final ProjectMapper projectMapper;

  public ProjectService(
      ProjectRepository projectRepository,
      UserRepository userRepository,
      CurrentUserProvider currentUserProvider,
      ProjectMapper projectMapper) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.currentUserProvider = currentUserProvider;
    this.projectMapper = projectMapper;
  }

  public ProjectResponse create(ProjectRequest request) {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    User owner =
        userRepository
            .findById(ownerId)
            .orElseThrow(() -> new NotFoundException("Owner user not found: " + ownerId));

    Project project = projectMapper.toNewEntity(request, owner);
    return projectMapper.toResponse(projectRepository.save(project));
  }

  @Transactional(readOnly = true)
  public List<ProjectResponse> list() {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    return projectRepository.findAllByOwnerId(ownerId).stream()
        .map(projectMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public Page<ProjectResponse> page(Pageable pageable) {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    return projectRepository.findAllByOwnerId(ownerId, pageable).map(projectMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ProjectResponse get(UUID projectId) {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    Project project =
        projectRepository
            .findByIdAndOwnerId(projectId, ownerId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
    return projectMapper.toResponse(project);
  }

  public ProjectResponse updateName(UUID projectId, ProjectRequest request) {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    Project project =
        projectRepository
            .findByIdAndOwnerId(projectId, ownerId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
    projectMapper.update(project, request);
    return projectMapper.toResponse(projectRepository.save(project));
  }

  public void delete(UUID projectId) {
    UUID ownerId = currentUserProvider.getCurrentUserId();
    Project project =
        projectRepository
            .findByIdAndOwnerId(projectId, ownerId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
    User owner = project.getOwner();
    owner.removeProject(project);
    userRepository.save(owner);
  }
}
