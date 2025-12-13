package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.entity.ids.ProjectMemberId;
import com.alok.projects.lovable_clone.enums.ProjectRole;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// this is the join table or mapping table that map user and project

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class ProjectMember {

    // because it is having 2 way mapping i.e. user_id and project_id combined make the primary key of ProjectMember table
    // just understand it as the id of ProjectMember where the type is of ProjectMemberId instead of Long or String or something else
    ProjectMemberId id;

    Project project;

    User user;

    ProjectRole projectRole;

    Instant invitedAt;
    Instant acceptedAt;
}
