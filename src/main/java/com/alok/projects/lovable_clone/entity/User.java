package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class User {

    @Id
    Long id;

    String email;

    String passwordHash;

    String name;

    String avatarUrl;

    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt; // soft delete (database entry will not be deleted)

    @OneToMany(mappedBy = "user")
    List<Subscription> subscriptions;

    @OneToMany(mappedBy = "user")
    List<UsageLog> usageLogs;

    @OneToMany(
            mappedBy = "user"
    )
    List<ProjectMember> projectMembers;

    @OneToMany(mappedBy = "owner")
    List<Project> ownedProjects;

    @OneToMany(
            mappedBy = "createdBy"
    )
    List<ProjectFile> createdFiles;

    @OneToMany(
            mappedBy = "updatedBy"
    )
    List<ProjectFile> updatedFiles;
}
