package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// individual files inside the project

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class ProjectFile {
    Long id;

    Project project;

    String path; // file path inside the project

    String minioObjectKey; // min io will be used to store the files;

    Instant createdAt;
    Instant updatedAt;

    // because the projects will be collaborative; multiple users can be added in the project member;
    User createdBy;
    User updatedBy;

}
