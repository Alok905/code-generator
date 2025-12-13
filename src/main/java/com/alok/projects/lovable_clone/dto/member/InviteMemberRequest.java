package com.alok.projects.lovable_clone.dto.member;

import com.alok.projects.lovable_clone.enums.ProjectRole;

public record InviteMemberRequest(
        String email,
        ProjectRole role
) {
}
