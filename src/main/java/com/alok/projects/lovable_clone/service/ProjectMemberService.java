package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.member.InvitationRespondRequest;
import com.alok.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.alok.projects.lovable_clone.dto.member.MemberResponse;
import com.alok.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.alok.projects.lovable_clone.dto.project.ProjectResponse;
import com.alok.projects.lovable_clone.enums.ProjectRole;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request);

    void removeProjectMember(Long projectId, Long memberId);

    void respondInvitation(Long id, InvitationRespondRequest request);
}
