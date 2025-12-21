package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String email;

    String passwordHash;

    String name;

    String avatarUrl;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt; // soft delete (database entry will not be deleted)

//    @OneToMany(mappedBy = "user")
//    List<Subscription> subscriptions;
//
//    @OneToMany(mappedBy = "user")
//    List<UsageLog> usageLogs;

//    @OneToMany(
//            mappedBy = "user"
//    )
//    List<ProjectMember> projectMembers;
//
//    @OneToMany(mappedBy = "owner")
//    List<Project> ownedProjects;
//
//    @OneToMany(
//            mappedBy = "createdBy"
//    )
//    List<ProjectFile> createdFiles;
//
//    @OneToMany(
//            mappedBy = "updatedBy"
//    )
//    List<ProjectFile> updatedFiles;
}
