package com.alok.projects.lovable_clone.dto.project;

import com.alok.projects.lovable_clone.dto.auth.UserProfileResponse;

import java.time.Instant;

// project to user: many to many; but project to user: one to one for the owner;
public record ProjectResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        UserProfileResponse owner
) {
}
