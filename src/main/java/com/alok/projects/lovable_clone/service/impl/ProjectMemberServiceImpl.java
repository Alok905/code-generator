package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.member.InvitationRespondRequest;
import com.alok.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.alok.projects.lovable_clone.dto.member.MemberResponse;
import com.alok.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.alok.projects.lovable_clone.entity.Project;
import com.alok.projects.lovable_clone.entity.ProjectMember;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.entity.ids.ProjectMemberId;
import com.alok.projects.lovable_clone.mapper.ProjectMemberMapper;
import com.alok.projects.lovable_clone.repository.ProjectMemberRepository;
import com.alok.projects.lovable_clone.repository.ProjectRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    ProjectMemberMapper projectMemberMapper;
    AuthUtil authUtil;

    @Override
    public List<MemberResponse> getProjectMembers(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);


        return projectMemberRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMemberMapper::toMemberResponseFromMember).toList();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId, userId);

        //TODO: it'll be handled after adding the authorization part
//        if(!project.getOwner().getId().equals(userId)) { /// only owner can invite
//            throw new RuntimeException("You are not allowed to invite.");
//        }

        User invitee = userRepository.findByUsername(request.username()).orElseThrow();

        if(invitee.getId().equals(userId)) {
            throw new RuntimeException("You cannot invite to yourself");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, invitee.getId());

        ProjectMember inviteeMember = projectMemberRepository.findById(projectMemberId).orElse(null);

        if(inviteeMember != null) {
            throw new RuntimeException("This user is already invited");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .user(invitee)
                .project(project)
                .invitedAt(Instant.now())
                .projectRole(request.role())
                .build();

        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toMemberResponseFromMember(projectMember);
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow();

        //TODO: it'll be handled after adding the authorization part
//        if(!project.getOwner().getId().equals(userId)) {
//            throw new RuntimeException("You are not allowed to update role.");
//        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();

        projectMember.setProjectRole(request.role());
        projectMemberRepository.save(projectMember);

        return projectMemberMapper.toMemberResponseFromMember(projectMember);
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId) {
        Project project = projectRepository.findById(projectId).orElseThrow();

        //TODO: it'll be handled after adding the authorization part
//        if(!project.getOwner().getId().equals(userId)) {
//            throw new RuntimeException("You are not allowed to remove project member.");
//        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        if(!projectMemberRepository.existsById(projectMemberId)) {
            throw new RuntimeException("This member doesn't exist");
        }

        projectMemberRepository.deleteById(projectMemberId);
    }

    @Override
    public void respondInvitation(Long id, InvitationRespondRequest request) {
        Long userId = authUtil.getCurrentUserId();

        Project project = projectRepository.findById(id).orElseThrow();

        ProjectMemberId projectMemberId = new ProjectMemberId(id, userId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();

        if(projectMember.getAcceptedAt() != null) {
            throw new RuntimeException("Invitation is already accepted");
        }

        if(!request.isAccepted()) {
            projectMemberRepository.deleteById(projectMemberId);
        } else {
            projectMember.setAcceptedAt(Instant.now());
            projectMemberRepository.save(projectMember);
        }
    }



    /// INTERNAL FUNCTIONS
    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId).orElseThrow();
    }

}
