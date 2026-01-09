package com.alok.projects.lovable_clone.dto.member;

import com.alok.projects.lovable_clone.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String username,
        String name,
//        String avatarUrl,
        ProjectRole projectRole,
        Instant invitedAt
) {
}
