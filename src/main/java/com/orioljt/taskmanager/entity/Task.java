package com.orioljt.taskmanager.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

import static com.orioljt.taskmanager.entity.TaskStatus.TODO;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TODO;

    private Integer priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
