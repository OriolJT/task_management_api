package com.orioljt.taskmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "users")
public class User implements Persistable<UUID> {

  @Id private UUID id;

  @Email
  @NotBlank
  @Size(max = 254)
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank
  @Size(min = 8, max = 100)
  @Column(nullable = false)
  private String password;

  @Column(name = "created_at")
  private Instant createdAt = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role = UserRole.USER;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Project> projects = new ArrayList<>();

  @Transient private boolean newRecord = true;

  @Override
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public List<Project> getProjects() {
    return projects;
  }

  public void addProject(Project project) {
    if (project == null) return;
    if (!this.projects.contains(project)) {
      this.projects.add(project);
    }
    project.setOwner(this);
  }

  public void removeProject(Project project) {
    if (project == null) return;
    this.projects.remove(project);
    if (project.getOwner() == this) {
      project.setOwner(null);
    }
  }

  @Override
  public boolean isNew() {
    return newRecord || id == null;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    this.newRecord = false;
  }

  public void markNew() {
    this.newRecord = true;
  }

  @PrePersist
  void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }
}
