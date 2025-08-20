package com.orioljt.taskmanager.mapper;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toNewEntity(ProjectRequest request, User owner) {
        Project project = new Project();
        project.setName(request.name());
        project.setOwner(owner);
        return project;
    }

    public void update(Project project, ProjectRequest request) {
        if (request.name() != null) {
            project.setName(request.name());
        }
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getOwner().getId(),
                project.getCreatedAt()
        );
    }
}
