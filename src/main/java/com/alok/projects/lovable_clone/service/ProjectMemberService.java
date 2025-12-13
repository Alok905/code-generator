package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.alok.projects.lovable_clone.dto.member.MemberResponse;
import com.alok.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.alok.projects.lovable_clone.enums.ProjectRole;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId, Long userId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, Long userId);

    MemberResponse deleteProjectMember(Long projectId, Long memberId, Long userId);
}
