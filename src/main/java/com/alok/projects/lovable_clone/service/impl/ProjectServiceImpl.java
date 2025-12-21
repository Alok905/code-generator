package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.project.ProjectRequest;
import com.alok.projects.lovable_clone.dto.project.ProjectResponse;
import com.alok.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.alok.projects.lovable_clone.entity.Project;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.mapper.ProjectMapper;
import com.alok.projects.lovable_clone.repository.ProjectRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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



    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow();

        Project project = Project.builder()
                .name(request.name())
                .owner(owner)
                .isPublic(false)
                .build();

        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        List<Project> projects = projectRepository.findAllAccessibleByUser(userId);

        return projects.stream()
                .map(projectMapper::toProjectSummaryResponse)
                .toList();
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        // we'll directly fetch from the db if user is member in that project
        Project project = getAccessibleProjectById(id, userId);
        return projectMapper.toProjectResponse(project);
    }


    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = getAccessibleProjectById(id, userId);
        if(!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete.");
        }
        project.setName(request.name());
        project = projectRepository.save(project); //NOTE: no need of saving; because after trnsaction, it'll do that by default
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        Project project = getAccessibleProjectById(id, userId);
        if(!project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete.");
        }
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }


    /// INTERNAL FUNCTIONS
    public Project getAccessibleProjectById(Long projectId, Long userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId).orElseThrow();
    }

}
