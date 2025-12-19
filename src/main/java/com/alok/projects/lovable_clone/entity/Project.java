package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

// its the whole project containing so many files (react project for example containing index.html, multiple .jsx files)
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class Project {
    @Id
    Long id;

    String name;

    @ManyToOne
    @JoinColumn(
            name = "owner_id"
    )
    User owner;

    Boolean isPublic = false;

    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt; // soft delete

    @OneToMany(mappedBy = "project")
    List<UsageLog> usageLogs;

    @OneToMany(
            mappedBy = "project"
    )
    List<ProjectMember> projectMembers;

//    @ManyToMany(mappedBy = "projects")
//    List<User> users;

    @OneToMany(mappedBy = "project")
    List<ProjectFile> projectFiles;

    @OneToOne(mappedBy = "project")
    Preview preview;
}
