package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

// individual files inside the project

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@Table(name = "project_files")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    Project project;

    @Column(nullable = false)
    String path; // file path inside the project

    String minioObjectKey; // min io will be used to store the files;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;
}
