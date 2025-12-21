package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.entity.ids.ProjectMemberId;
import com.alok.projects.lovable_clone.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// this is the join table or mapping table that map user and project

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "project_members")
@ToString
public class ProjectMember {

    // because it is having 2 way mapping i.e. user_id and project_id combined make the primary key of ProjectMember table
    // just understand it as the id of ProjectMember where the type is of ProjectMemberId instead of Long or String or something else
    @EmbeddedId
    ProjectMemberId id;

    @ManyToOne
    @MapsId("projectId") // we don't want another column for ProjectMemberId; we just want to map our 2 columns (user & project) to ProjectMember's fields (userId & projectId)
    @JoinColumn(
            name = "project_id"
    )
    Project project;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(
            name = "user_id"
    )
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false)
    ProjectRole projectRole;

    Instant invitedAt;
    Instant acceptedAt;
}
