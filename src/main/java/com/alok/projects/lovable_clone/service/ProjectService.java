package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.project.ProjectRequest;
import com.alok.projects.lovable_clone.dto.project.ProjectResponse;
import com.alok.projects.lovable_clone.dto.project.ProjectSummaryResponse;

import java.util.List;


public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects();

    ProjectResponse getUserProjectById(Long projectId);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(Long id, ProjectRequest request);

    void softDelete(Long id);

}
