package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.project.ProjectRequest;
import com.alok.projects.lovable_clone.dto.project.ProjectResponse;
import com.alok.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.alok.projects.lovable_clone.entity.Project;
import com.alok.projects.lovable_clone.entity.ProjectMember;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.entity.ids.ProjectMemberId;
import com.alok.projects.lovable_clone.enums.ProjectRole;
import com.alok.projects.lovable_clone.error.BadRequestException;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.mapper.ProjectMapper;
import com.alok.projects.lovable_clone.repository.ProjectMemberRepository;
import com.alok.projects.lovable_clone.repository.ProjectRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.ProjectService;
import com.alok.projects.lovable_clone.service.ProjectTemplateService;
import com.alok.projects.lovable_clone.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;
    SubscriptionService subscriptionService;
    ProjectTemplateService projectTemplateService;



    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        if (!subscriptionService.canCreateNewProject()) {
            throw new BadRequestException("User cannot create a new project with current Plan, Upgrade now");
        }


        Long userId = authUtil.getCurrentUserId();

        /// because we only need the user's id, not everything. so getting reference is enough.
        User owner = userRepository.getReferenceById(userId);

        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), userId);
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .projectRole(ProjectRole.OWNER)
                .project(project)
                .user(owner)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .build();

        projectMemberRepository.save(projectMember);

        projectTemplateService.initializeProjectFromTemplate(project.getId());

        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        List<Project> projects = projectRepository.findAllAccessibleByUser(userId);

        return projects.stream()
                .map(projectMapper::toProjectSummaryResponse)
                .toList();
    }

    @Override
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectResponse getUserProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        // we'll directly fetch from the db if user is member in that project
        Project project = getAccessibleProjectById(projectId, userId);
        return projectMapper.toProjectResponse(project);
    }


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id, userId);

        //TODO: it'll be handled after adding the authorization part
//        if(!project.getOwner().getId().equals(userId)) {
//            throw new RuntimeException("You are not allowed to delete.");
//        }

        project.setName(request.name());
        project = projectRepository.save(project); //NOTE: no need of saving; because after transaction, it'll do that by default
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#projectId)")
    public void softDelete(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id, userId);

        //TODO: it'll be handled after adding the authorization part
//        if(!project.getOwner().getId().equals(userId)) {
//            throw new RuntimeException("You are not allowed to delete.");
//        }

        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }



    /// INTERNAL FUNCTIONS
    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));
    }

}
