package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.ProjectResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.UserRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    private ProjectService service;

    private UUID userId;
    private User owner;

    @BeforeEach
    void setUp() {
        service = new ProjectService(projectRepository, userRepository, currentUserProvider, new com.orioljt.taskmanager.mapper.ProjectMapper());
        userId = UUID.randomUUID();
        owner = new User();
        owner.setId(userId);
        owner.setEmail("user@example.com");
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    }

    @Test
    void create_shouldPersistProjectOwnedByCurrentUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        when(projectRepository.save(any())).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProjectRequest req = new ProjectRequest("My Project");
        ProjectResponse res = service.create(req);

        verify(projectRepository).save(captor.capture());
        Project saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("My Project");
        assertThat(saved.getOwner().getId()).isEqualTo(userId);
        assertThat(res.name()).isEqualTo("My Project");
        assertThat(res.ownerId()).isEqualTo(userId);
    }

    @Test
    void list_shouldReturnOwnerProjects() {
        Project p = new Project();
        p.setId(UUID.randomUUID());
        p.setName("P1");
        p.setOwner(owner);
        when(projectRepository.findAllByOwnerId(userId)).thenReturn(List.of(p));

        List<ProjectResponse> list = service.list();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).name()).isEqualTo("P1");
    }

    @Test
    void get_shouldReturnOwnedProject() {
        UUID pid = UUID.randomUUID();
        Project p = new Project();
        p.setId(pid);
        p.setName("P1");
        p.setOwner(owner);
        when(projectRepository.findByIdAndOwnerId(pid, userId)).thenReturn(Optional.of(p));

        ProjectResponse res = service.get(pid);
        assertThat(res.id()).isEqualTo(pid);
        assertThat(res.ownerId()).isEqualTo(userId);
    }

    @Test
    void get_shouldThrowIfNotOwned() {
        UUID pid = UUID.randomUUID();
        when(projectRepository.findByIdAndOwnerId(pid, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(pid)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateName_shouldPersist() {
        UUID pid = UUID.randomUUID();
        Project p = new Project();
        p.setId(pid);
        p.setName("Old");
        p.setOwner(owner);
        when(projectRepository.findByIdAndOwnerId(pid, userId)).thenReturn(Optional.of(p));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectRequest req = new ProjectRequest("New");
        ProjectResponse res = service.updateName(pid, req);
        assertThat(res.name()).isEqualTo("New");
    }

    @Test
    void updateName_shouldThrowIfNotOwned() {
        UUID pid = UUID.randomUUID();
        when(projectRepository.findByIdAndOwnerId(pid, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateName(pid, new ProjectRequest("X")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldRemoveOwnedProject() {
        UUID pid = UUID.randomUUID();
        Project p = new Project();
        p.setId(pid);
        p.setOwner(owner);
        when(projectRepository.findByIdAndOwnerId(pid, userId)).thenReturn(Optional.of(p));

        service.delete(pid);
        verify(projectRepository).delete(p);
    }

    @Test
    void delete_shouldThrowIfNotOwned() {
        UUID pid = UUID.randomUUID();
        when(projectRepository.findByIdAndOwnerId(pid, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(pid)).isInstanceOf(NotFoundException.class);
    }
}