package com.alok.projects.lovable_clone.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static com.alok.projects.lovable_clone.enums.ProjectPermission.*;

//@RequiredArgsConstructor
@Getter
public enum ProjectRole {
    EDITOR(VIEW, EDIT, DELETE, VIEW_MEMBERS),
    VIEWER(VIEW, VIEW_MEMBERS),
    OWNER(VIEW, EDIT, DELETE, VIEW_MEMBERS, MANAGE_MEMBERS);

    ProjectRole(ProjectPermission... permissions) {
        this.permissions = Set.of(permissions);
    }


    private final Set<ProjectPermission> permissions;
}