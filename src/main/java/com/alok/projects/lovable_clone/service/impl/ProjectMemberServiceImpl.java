package com.alok.projects.lovable_clone.service.impl;

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
import com.alok.projects.lovable_clone.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    ProjectMemberMapper projectMemberMapper;

    @Override
    public List<MemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        List<MemberResponse> memberResponseList = new ArrayList<>();

        // add the owner; because owner is also a member
        User owner = project.getOwner();
        memberResponseList.add(projectMemberMapper.toMemberResponseFromOwner(owner));

        // now add the members;
        List<ProjectMember> projectMembers = projectMemberRepository.findByIdProjectId(projectId);
        List<MemberResponse> memberResponses = projectMembers.stream().map(projectMemberMapper::toMemberResponseFromMember).toList();
        memberResponseList.addAll(memberResponses);

        return memberResponseList;
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId) {
        Project project = getAccessibleProjectById(projectId, userId);

        if(!project.getOwner().getId().equals(userId)) { // if user is owner then only he can invite
            throw new RuntimeException("You are not allowed to invite.");
        }

        User invitee = userRepository.findByEmail(request.email()).orElseThrow();

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
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, Long userId) {
        Project project = projectRepository.findById(projectId).orElseThrow();

        if(!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to update role.");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();

//        if(projectMember.getProjectRole().equals(request.role())) {
//            throw new RuntimeException("This member has the same role already");
//        }

        projectMember.setProjectRole(request.role());
        projectMemberRepository.save(projectMember);

        return projectMemberMapper.toMemberResponseFromMember(projectMember);
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId, Long userId) {
        Project project = projectRepository.findById(projectId).orElseThrow();

        if(!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to remove project member.");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        if(!projectMemberRepository.existsById(projectMemberId)) {
            throw new RuntimeException("This member doesn't exist");
        }

        projectMemberRepository.deleteById(projectMemberId);
    }

    /// INTERNAL FUNCTIONS
    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId).orElseThrow();
    }

}
