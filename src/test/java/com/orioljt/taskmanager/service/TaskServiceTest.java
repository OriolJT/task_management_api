package com.orioljt.taskmanager.service;

import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.Task;
import com.orioljt.taskmanager.entity.TaskStatus;
import com.orioljt.taskmanager.exception.NotFoundException;
import com.orioljt.taskmanager.repository.ProjectRepository;
import com.orioljt.taskmanager.repository.TaskRepository;
import com.orioljt.taskmanager.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private CurrentUserProvider currentUserProvider;

    private TaskService service;
    private UUID userId;
    private UUID projectId;
    private Project ownedProject;

    @BeforeEach
    void setUp() {
        service = new TaskService(taskRepository, projectRepository, currentUserProvider, new com.orioljt.taskmanager.mapper.TaskMapper());
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        ownedProject = new Project();
        ownedProject.setId(projectId);
        when(projectRepository.findByIdAndOwnerId(projectId, userId)).thenReturn(Optional.of(ownedProject));
    }

    @Test
    void create_shouldPersistTaskWithDefaults() {
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(any())).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });
        TaskRequest req = new TaskRequest("Title", null, null, 3, LocalDate.now().plusDays(1));
        TaskResponse res = service.create(projectId, req);

        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(saved.getProject().getId()).isEqualTo(projectId);
        assertThat(res.projectId()).isEqualTo(projectId);
        assertThat(res.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void create_shouldThrowIfProjectNotOwned() {
        UUID otherProject = UUID.randomUUID();
        when(projectRepository.findByIdAndOwnerId(otherProject, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(otherProject, new TaskRequest("T", null, null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void list_shouldReturnTasksOrdered() {
        Task t = new Task();
        t.setId(UUID.randomUUID());
        t.setTitle("T");
        t.setProject(ownedProject);
        when(taskRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId)).thenReturn(List.of(t));

        List<TaskResponse> list = service.list(projectId);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().title()).isEqualTo("T");
    }

    @Test
    void get_shouldReturnTaskWhenOwned() {
        UUID taskId = UUID.randomUUID();
        Task t = new Task();
        t.setId(taskId);
        t.setTitle("T");
        t.setProject(ownedProject);
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.of(t));

        TaskResponse res = service.get(projectId, taskId);
        assertThat(res.id()).isEqualTo(taskId);
    }

    @Test
    void get_shouldThrowWhenNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(projectId, taskId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldApplyNonNullFields() {
        UUID taskId = UUID.randomUUID();
        Task t = new Task();
        t.setId(taskId);
        t.setProject(ownedProject);
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.of(t));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TaskRequest req = new TaskRequest("New", "Desc", TaskStatus.DONE, 1, LocalDate.now());
        TaskResponse res = service.update(projectId, taskId, req);
        assertThat(res.title()).isEqualTo("New");
        assertThat(res.description()).isEqualTo("Desc");
        assertThat(res.status()).isEqualTo(TaskStatus.DONE);
        assertThat(res.priority()).isEqualTo(1);
    }

    @Test
    void update_shouldThrowWhenNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(projectId, taskId, new TaskRequest(null, null, null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldRemoveTaskWhenOwned() {
        UUID taskId = UUID.randomUUID();
        Task t = new Task();
        t.setId(taskId);
        t.setProject(ownedProject);
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.of(t));

        service.delete(projectId, taskId);
        verify(taskRepository).delete(t);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(projectId, taskId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void requireOwnedProject_shouldThrowIfNotOwned() {
        UUID otherProject = UUID.randomUUID();
        when(projectRepository.findByIdAndOwnerId(otherProject, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.list(otherProject)).isInstanceOf(NotFoundException.class);
    }
}
