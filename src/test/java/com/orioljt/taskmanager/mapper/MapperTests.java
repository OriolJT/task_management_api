package com.orioljt.taskmanager.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.orioljt.taskmanager.dto.ProjectRequest;
import com.orioljt.taskmanager.dto.TaskRequest;
import com.orioljt.taskmanager.dto.TaskResponse;
import com.orioljt.taskmanager.dto.UpdateUserRequest;
import com.orioljt.taskmanager.entity.Project;
import com.orioljt.taskmanager.entity.Task;
import com.orioljt.taskmanager.entity.TaskStatus;
import com.orioljt.taskmanager.entity.User;
import com.orioljt.taskmanager.entity.UserRole;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MapperTests {

  @Test
  void projectMapper_toNewEntity_setsOwnerAndName() {
    ProjectMapper mapper = new ProjectMapper();
    User owner = new User();
    owner.setId(UUID.randomUUID());
    Project p = mapper.toNewEntity(new ProjectRequest("MyProj"), owner);
    assertThat(p.getName()).isEqualTo("MyProj");
    assertThat(p.getOwner()).isSameAs(owner);
    assertThat(owner.getProjects()).contains(p);
  }

  @Test
  void projectMapper_update_changesName_whenProvided() {
    ProjectMapper mapper = new ProjectMapper();
    Project p = new Project();
    p.setName("Old");
    mapper.update(p, new ProjectRequest("New"));
    assertThat(p.getName()).isEqualTo("New");
  }

  @Test
  void taskMapper_toNewEntity_defaultsStatusAndLinksProject() {
    TaskMapper mapper = new TaskMapper();
    Project project = new Project();
    TaskRequest req = new TaskRequest("Title", "Desc", null, 3, LocalDate.now());
    Task t = mapper.toNewEntity(req, project);
    assertThat(t.getProject()).isSameAs(project);
    assertThat(project.getTasks()).contains(t);
    assertThat(t.getStatus()).isEqualTo(TaskStatus.TODO);
  }

  @Test
  void taskMapper_updateEntity_updatesNonNullFields() {
    TaskMapper mapper = new TaskMapper();
    Task t = new Task();
    t.setTitle("A");
    t.setStatus(TaskStatus.TODO);
    mapper.updateEntity(t, new TaskRequest("B", null, TaskStatus.DONE, null, null));
    assertThat(t.getTitle()).isEqualTo("B");
    assertThat(t.getStatus()).isEqualTo(TaskStatus.DONE);
  }

  @Test
  void taskMapper_toResponse_includesProjectId() {
    TaskMapper mapper = new TaskMapper();
    Project p = new Project();
    p.setId(UUID.randomUUID());
    Task t = new Task();
    p.addTask(t);
    TaskResponse resp = mapper.toResponse(t);
    assertThat(resp.projectId()).isEqualTo(p.getId());
  }

  @Test
  void userMapper_updatesEmailAndRole() {
    UserMapper mapper = new UserMapper();
    User u = new User();
    u.setEmail("a@b.com");
    u.setRole(UserRole.USER);
  mapper.update(u, new UpdateUserRequest("c@d.com", UserRole.ADMIN, null));
    assertThat(u.getEmail()).isEqualTo("c@d.com");
    assertThat(u.getRole()).isEqualTo(UserRole.ADMIN);
  }
}
