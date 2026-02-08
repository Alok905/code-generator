package com.alok.projects.lovable_clone.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

/// It's the whole project containing so many files (react project for example containing index.html, multiple .jsx files)
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) /// it'll make the fields private by default
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "projects",
        indexes = {
                @Index(name = "idx_projects_updated_at_desc", columnList = "updated_at DESC, deleted_at"),
                @Index(name = "idx_projects_deleted_at_desc", columnList = "deleted_at")
        }
)
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

//    @ManyToOne
//    @JoinColumn(
//            name = "owner_id",
//            nullable = false
//    )
//    User owner;

    Boolean isPublic = false;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt; // soft delete

    @OneToMany(mappedBy = "project")
    List<UsageLog> usageLogs;

    @OneToMany(
            mappedBy = "project"
    )
    List<ProjectMember> projectMembers;

    @OneToMany(mappedBy = "project")
    List<ProjectFile> projectFiles;

    @OneToOne(mappedBy = "project")
    Preview preview;
}
